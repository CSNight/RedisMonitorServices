package csnight.redis.monitor.quartz.jobs;

import com.alibaba.fastjson.JSONObject;
import com.csnight.jedisql.JediSQL;
import csnight.redis.monitor.msg.MsgBus;
import csnight.redis.monitor.msg.entity.ChannelEntity;
import csnight.redis.monitor.msg.entity.WssResponseEntity;
import csnight.redis.monitor.msg.series.RedisCmdType;
import csnight.redis.monitor.msg.series.ResponseMsgType;
import csnight.redis.monitor.redis.data.ResultParser;
import csnight.redis.monitor.redis.pool.MultiRedisPool;
import csnight.redis.monitor.redis.pool.RedisPoolInstance;
import csnight.redis.monitor.utils.IdentifyUtils;
import csnight.redis.monitor.websocket.WebSocketServer;
import org.quartz.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@PersistJobDataAfterExecution
public class Job_CommandExec implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        JobKey jobKey = context.getJobDetail().getKey();
        Map<String, String> params = (Map<String, String>) jobDataMap.get("params");
        RedisPoolInstance pool = MultiRedisPool.getInstance().getPool(params.get("ins_id"));
        if (pool == null) {
            try {
                context.getScheduler().pauseJob(jobKey);
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
            return;
        }
        String clientId = IdentifyUtils.getUUID();
        JediSQL jediSQL;
        try {
            jediSQL = pool.getJedis(clientId);
        } catch (Exception ex) {
            return;
        }
        if (jediSQL == null) {
            return;
        }
        String cmd = params.get("exe");
        Object response;
        long start = System.currentTimeMillis();
        try {
            int db = Integer.parseInt(params.get("db"));
            if (jediSQL.select(db).equals("OK")) {
                response = execute(cmd, jediSQL);
            } else {
                response = "Select Logic Database Failure";
            }
        } catch (Exception ex) {
            response = ex.getMessage() == null ? "Null response exception" : ex.getMessage();
        } finally {
            pool.close(clientId);
        }
        long end = System.currentTimeMillis();
        long times = Long.parseLong(params.get("times"));
        if (context.getTrigger() instanceof SimpleTrigger) {
            times = ((SimpleTrigger) context.getTrigger()).getTimesTriggered();
        } else {
            times = times + 1;
        }
        params.put("times", String.valueOf(times));
        jobDataMap.put("params", params);
        //存在ID为空的 直接返回
        if (params.get("cid").equals("") || params.get("appId").equals("")) {
            response = null;
            return;
        }
        //channelId不为空 则查询通道实体
        ChannelEntity che = MsgBus.getIns().getChannels().get(params.get("cid"));
        if (che == null) {
            //通道实体为空时,清空无效的channelId及appId
            params.put("cid", "");
            params.put("appId", "");
            response = null;
            return;
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("response", response == null ? "" : response);
        resultMap.put("times", times);
        resultMap.put("jobId", jobKey.getName());
        WssResponseEntity wre = new WssResponseEntity(ResponseMsgType.RMS_EXEC, resultMap, end - start);
        wre.setAppId(params.get("appId"));
        WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), che.getChannel());
        resultMap.clear();
        resultMap = null;
    }

    public Object execute(String cmd, JediSQL jediSQL) {
        if (cmd != null && !cmd.equals("")) {
            String[] parts = cmd.split(" ");
            List<String> ps = new LinkedList<>();
            for (String p : parts) {
                if (!p.equals("")) {
                    ps.add(p);
                }
            }
            parts = ps.toArray(new String[]{});
            ps.clear();
            ps = null;
            RedisCmdType command = RedisCmdType.getEnumType(parts[0].toUpperCase());
            byte[][] args;
            if (parts.length > 1) {
                args = new byte[parts.length - 1][];
                for (int i = 1; i < parts.length; i++) {
                    args[i - 1] = parts[i].getBytes();
                }
            } else {
                args = new byte[][]{};
            }
            Object res = jediSQL.sendCommand(command, args);
            return ResultParser.MsgParser(res);
        }
        return "Command Not Set";
    }
}
