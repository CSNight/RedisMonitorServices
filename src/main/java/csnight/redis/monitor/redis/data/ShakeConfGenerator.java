package csnight.redis.monitor.redis.data;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import csnight.redis.monitor.utils.BaseUtils;
import csnight.redis.monitor.utils.IdentifyUtils;
import csnight.redis.monitor.utils.YamlConfigUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author csnight
 * @description
 * @since 2020-1-18 22:12
 */
public class ShakeConfGenerator {
    private final static String Root = System.getProperty("user.dir");
    private String[] BaseConf = new String[]{"log.file=", "log.level = info", "parallel = %d",
            "fake_time =", "rewrite = %b", "big_key_threshold = 524288000", "psync = true",
            "qps = 200000", "keep_alive = 0", "replace_hash_tag = false", "metric = true",
            "metric.print_log = true"};
    private String[] SourceConf = new String[]{"source.type = %s", "source.address = %s",
            "source.password_raw = %s", "source.auth_type = auth",
            "source.tls_enable = %b", "source.rdb.input = %s",
            "source.rdb.parallel = %d", "source.rdb.special_cloud ="};
    private String[] TargetConf = new String[]{"target.type = %s", "target.address = %s",
            "target.password_raw = %s", "target.auth_type = auth",
            "target.tls_enable = %b", "target.rdb.output = %s",
            "target.db = %d", "target.version ="};
    private String[] FilterConf = new String[]{"filter.db.whitelist = %s",
            "filter.db.blacklist = %s", "filter.key.whitelist = %s",
            "filter.key.blacklist = %s", "filter.slot = %s", "filter.lua =  %b"};
    private String[] SenderConf = new String[]{"sender.size = 104857600", "sender.count = 4095", "sender.delay_channel_size = 65535"};
    private String[] ScanConf = new String[]{"scan.key_number = %d", "scan.special_cloud =", "scan.key_file ="};
    private List<String> confTemplate = new ArrayList<>();

    private boolean FormatTemplate(JSONObject configs) {
        boolean base = FormatTemplate(configs, BaseConf);
        boolean source = FormatTemplate(configs, SourceConf);
        boolean target = FormatTemplate(configs, TargetConf);
        boolean filter = FormatTemplate(configs, FilterConf);
        boolean sender = FormatTemplate(configs, SenderConf);
        boolean scan = FormatTemplate(configs, ScanConf);
        Boolean[] res = new Boolean[]{base, source, target, filter, sender, scan};
        return BaseUtils.any(new ArrayList<>(Arrays.asList(res)));
    }

    private boolean FormatTemplate(JSONObject jo, String[] template) {
        for (int i = 0; i < template.length; i++) {
            if (template[i].contains("%")) {
                Object val = JSONPath.eval(jo, "$." + template[i].split(" ")[0]);
                if (val == null) {
                    if (template[i].contains("%s")) {
                        val = "";
                    } else if (template[i].contains("%b")) {
                        val = false;
                    } else if (template[i].contains("%d")) {
                        val = 0;
                    }
                }
                if (template[i].contains("input") || template[i].contains("output")) {
                    String recordDir = Root + "/" + YamlConfigUtils.getStrYmlVal("dumpdir.record-dir");
                    template[i] = String.format(template[i], recordDir + "/" + val);
                } else {
                    template[i] = String.format(template[i], val);
                }
            }
        }
        confTemplate.addAll(Arrays.asList(template));
        return true;
    }

    public String GenerateFile(JSONObject configs) throws IOException {
        boolean temp_res = FormatTemplate(configs);
        String confDir = Root + "/" + YamlConfigUtils.getStrYmlVal("dumpdir.conf-dir") + "/";
        File fileDir = new File(confDir);
        if (!fileDir.exists()) {
            boolean suc = fileDir.mkdir();
            if (!suc) {
                return null;
            }
        }
        String filename = IdentifyUtils.string2MD5(IdentifyUtils.getUUID2() + "-" + new Date().getTime(), "Conf$");
        File confFile = new File(confDir + filename + ".conf");
        if (!confFile.exists()) {
            boolean res = confFile.createNewFile();
            if (res) {
                FileWriter fileWriter = new FileWriter(confFile);
                BufferedWriter bw = new BufferedWriter(fileWriter);
                for (String line : confTemplate) {
                    bw.write(line);
                    bw.newLine();
                }
                bw.flush();
                bw.close();
                return confFile.getName();
            }
        }
        return null;
    }

    public static boolean clearConf(String filename) {
        String confDir = Root + "/" + YamlConfigUtils.getStrYmlVal("dumpdir.conf-dir") + "/";
        File file = new File(confDir + filename);
        if (file.exists()) {
            return file.delete();
        }
        return true;
    }
}
