package csnight.redis.monitor.redis.data;

import csnight.redis.monitor.utils.YamlConfigUtils;

/**
 * @author csnight
 * @description
 * @since 2020-1-18 22:12
 */
public class ShakeConfGenerator {
    private final static String TEMP_CONF_DIR = ShakeConfGenerator.class.getResource("").getPath()
            + YamlConfigUtils.getStrYmlVal("dumpdir.conf-dir");
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

}
