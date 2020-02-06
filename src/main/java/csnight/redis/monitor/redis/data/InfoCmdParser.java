package csnight.redis.monitor.redis.data;

import com.csnight.jedisql.JediSQL;
import csnight.redis.monitor.msg.series.RedisCmdType;
import csnight.redis.monitor.redis.pool.RedisPoolInstance;
import csnight.redis.monitor.utils.BaseUtils;
import csnight.redis.monitor.utils.IdentifyUtils;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class InfoCmdParser {
    private static final String SECTIONS = String.join(",", new String[]{"Server", "Clients", "Memory", "Persistence", "Stats", "Replication", "CPU", "Cluster", "Keyspace", "Sentinel"});

    public static String GetInfoBySectionKey(RedisPoolInstance pool, String section, String key) {
        if (SECTIONS.contains(section)) {
            return GetInstanceInfo(pool, section).get(key);
        }
        return "";
    }

    public static Map<String, Map<String, String>> GetInfoAll(RedisPoolInstance pool) {
        Map<String, Map<String, String>> infos = new HashMap<>();
        String jid = IdentifyUtils.getUUID();
        JediSQL j = pool.getJedis(jid);
        String[] sections = SECTIONS.split(",");
        for (String section : sections) {
            Map<String, String> sectionInfo = new HashMap<>();
            String[] info = GetInfoSection(j, section);
            for (String item : info) {
                String[] kv = item.split(":");
                if (item.contains("executable:") || item.contains("config_file:")) {
                    sectionInfo.put(kv[0], item.substring(kv[0].length() + 1));
                } else {
                    sectionInfo.put(kv[0], String.valueOf(kv[1]));
                }
            }
            infos.put(section, sectionInfo);
        }
        pool.close(jid);
        return infos;
    }

    public static String[] GetInfoSection(JediSQL j, String section) {
        Object infos = j.sendCommand(RedisCmdType.INFO, section);
        String encoding = BaseUtils.getEncoding((byte[]) infos);
        String tmp = new String((byte[]) infos, Charset.forName(encoding)).replaceAll("# " + section + "\\r\\n", "").replaceAll("\\r\\n", ";");
        if (SECTIONS.contains(section)) {
            int spiltIndex = tmp.lastIndexOf(";");
            if (tmp.length() > 0 && spiltIndex != -1) {
                return tmp.substring(0, spiltIndex).split(";");
            }
        }
        return new String[]{};
    }

    public static Map<String, String> GetInstanceInfo(RedisPoolInstance pool, String section) {
        String jid = IdentifyUtils.getUUID();
        JediSQL j = pool.getJedis(jid);
        Map<String, String> info = new HashMap<>();
        String[] infos = GetInfoSection(j, section);
        pool.close(jid);
        for (String item : infos) {
            String[] kv = item.split(":");
            if (item.contains("executable:") || item.contains("config_file:")) {
                info.put(kv[0], item.substring(kv[0].length() + 1));
            } else {
                info.put(kv[0], String.valueOf(kv[1]));
            }
        }
        return info;
    }
}
