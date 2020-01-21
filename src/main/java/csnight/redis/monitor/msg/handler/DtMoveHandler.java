package csnight.redis.monitor.msg.handler;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import csnight.redis.monitor.db.jpa.RmsDataRecord;
import csnight.redis.monitor.db.jpa.RmsShakeRecord;
import csnight.redis.monitor.db.repos.RmsDataRecRepository;
import csnight.redis.monitor.db.repos.RmsShakeRepository;
import csnight.redis.monitor.msg.entity.WssResponseEntity;
import csnight.redis.monitor.msg.series.ResponseMsgType;
import csnight.redis.monitor.utils.ReflectUtils;
import csnight.redis.monitor.utils.YamlConfigUtils;
import csnight.redis.monitor.websocket.WebSocketServer;
import io.netty.channel.Channel;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Optional;

/**
 * @author csnight
 * @description
 * @since 2020/1/20 09:23
 */
public class DtMoveHandler implements WsChannelHandler {
    private String appId;
    private Channel channel;
    private Process process;
    private String sid = "";
    private JSONObject configs;


    public DtMoveHandler(String appId, Channel ch) {
        this.appId = appId;
        this.channel = ch;
    }

    @Override
    public String getIns() {
        return null;
    }

    @Override
    public void initialize(JSONObject msg) {
        JSONObject shakeConf = JSONObject.parseObject(msg.getString("msg"));
        String mode = shakeConf.getString("shake_type");
        configs = JSONObject.parseObject(shakeConf.getString("conf"));
        sid = shakeConf.getString("id");
        String confFile = shakeConf.getString("filepath");
        String path = System.getProperty("user.dir") + "/" + YamlConfigUtils.getStrYmlVal("dumpdir.exec-dir") + "/";
        String os = System.getProperty("os.name");
        String config = System.getProperty("user.dir") + "/" + YamlConfigUtils.getStrYmlVal("dumpdir.conf-dir") + "/" + confFile;
        String cmdLine;
        if (os.toLowerCase().contains("windows")) {
            path += "redis-shake.windows.exe";
            cmdLine = path;
        } else if (os.toLowerCase().contains("mac")) {
            path += "redis-shake.darwin";
            cmdLine = "./" + path;
        } else {
            path += "redis-shake.linux";
            cmdLine = "./" + path;
        }
        Thread thread = new Thread(() -> {
            try {
                execute(cmdLine, mode, config);
            } catch (IOException e) {
                WssResponseEntity wre = new WssResponseEntity(ResponseMsgType.ERROR, "Process stop with an error " + e.getMessage());
                wre.setAppId(appId);
                WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), channel);
                WssResponseEntity wres = new WssResponseEntity(ResponseMsgType.SHAKEFINISH, "Data process finished");
                wres.setAppId(appId);
                WebSocketServer.getInstance().send(JSONObject.toJSONString(wres), channel);
            }
        });
        thread.start();
    }

    private void execute(String cmdLine, String mode, String config) throws IOException {
        BufferedReader br = null;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(cmdLine, "-type=" + mode, "-conf=" + config);
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();
            br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String inline;
            while ((inline = br.readLine()) != null) {
                if (inline.equals("")) {
                    inline = "\r\n";
                }
                WssResponseEntity wre = new WssResponseEntity(ResponseMsgType.SHAKEPROCESS, inline);
                wre.setAppId(appId);
                WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), channel);
            }
            process.waitFor();
            SaveResult("success");
        } catch (InterruptedException e) {
            WssResponseEntity wre = new WssResponseEntity(ResponseMsgType.ERROR, "Process stop with an error " + e.getMessage());
            wre.setAppId(appId);
            WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), channel);
            SaveResult("failed");
        } finally {
            if (br != null)
                br.close();
            File configFile = new File(config);
            if (configFile.exists()) {
                configFile.delete();
            }
        }
    }

    private void SaveResult(String result) {
        RmsShakeRepository shakeRepository = ReflectUtils.getBean(RmsShakeRepository.class);
        RmsDataRecRepository dataRecRepository = ReflectUtils.getBean(RmsDataRecRepository.class);
        Optional<RmsShakeRecord> optShakeRecord = shakeRepository.findById(sid);
        RmsDataRecord dataRecord = new RmsDataRecord();
        if (optShakeRecord.isPresent()) {
            RmsShakeRecord shakeRecord = optShakeRecord.get();
            String user = shakeRecord.getCreate_user();
            String mode = shakeRecord.getShake_type();
            switch (mode) {
                case "dump":
                case "decode":
                    if (result.equals("success")) {
                        String output = JSONPath.eval(configs, "$.target.rdb.output").toString();
                        long fileSize = CheckOutput(output);
                        if (fileSize != 0L) {
                            dataRecord.setIns_id(JSONPath.eval(configs, "$.sourceId").toString());
                            dataRecord.setBackup_type(mode);
                            dataRecord.setCreate_time(new Date());
                            dataRecord.setCreate_user(user);
                            dataRecord.setSize(fileSize);
                            dataRecord.setFilename(new File(output).getName());
                            RmsDataRecord rmsDataRecord = dataRecRepository.save(dataRecord);
                            shakeRecord.setRelate_backup(rmsDataRecord.getId());
                        } else {
                            result = "failed";
                        }
                    }
                    shakeRecord.setResult(result);
                    shakeRepository.save(shakeRecord);
                    break;
                case "restore":
                    String input = JSONPath.eval(configs, "$.source.rdb.input").toString();
                    RmsDataRecord dataInput = dataRecRepository.findByFilename(new File(input).getName());
                    if (dataInput != null) {
                        shakeRecord.setRelate_backup(dataInput.getId());
                    }
                    shakeRecord.setResult(result);
                    shakeRepository.save(shakeRecord);
                case "rump":
                case "sync":
                    shakeRecord.setResult(result);
                    shakeRepository.save(shakeRecord);
                    break;
            }
            WssResponseEntity wres = new WssResponseEntity(ResponseMsgType.SHAKEFINISH, "Data process finished");
            wres.setAppId(appId);
            WebSocketServer.getInstance().send(JSONObject.toJSONString(wres), channel);
        }
    }

    private long CheckOutput(String output) {
        String outputFile = System.getProperty("user.dir") + "/"
                + YamlConfigUtils.getStrYmlVal("dumpdir.record-dir") + "/" + output;
        File outputF = new File(outputFile);
        if (outputF.exists()) {
            return outputF.length();
        }
        return 0L;
    }

    @Override
    public void destroy() {
        if (process != null) {
            process.destroy();
        }
        System.gc();
    }
}
