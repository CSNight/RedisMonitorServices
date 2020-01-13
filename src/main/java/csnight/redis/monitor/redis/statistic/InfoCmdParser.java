package csnight.redis.monitor.redis.statistic;

import com.csnight.jedisql.JediSQL;
import csnight.redis.monitor.msg.series.RedisCmdType;
import csnight.redis.monitor.redis.pool.RedisPoolInstance;
import csnight.redis.monitor.utils.BaseUtils;
import csnight.redis.monitor.utils.IdentifyUtils;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class InfoCmdParser {
    private static final String SECTIONS = String.join(",", new String[]{"Server", "Clients", "Memory", "Persistence", "Stats", "Replication", "CPU", "Cluster", "Keyspace"});

    public static String GetInfoBySectionKey(RedisPoolInstance pool, String section, String key) {
        if (SECTIONS.contains(section)) {
            return GetInstanceInfo(pool, section).get(key);
        }
        return "";
    }

    private static String GetInfoAll(RedisPoolInstance pool) {
        String jid = IdentifyUtils.getUUID();
        JediSQL j = pool.getJedis(jid);
        Object tmp = j.sendCommand(RedisCmdType.INFO,"");
        String encoding = BaseUtils.getEncoding((byte[]) tmp);
        String infos = new String((byte[]) tmp, Charset.forName(encoding));
        pool.close(jid);
        return infos;
    }

    private static String[] GetInfoSection(RedisPoolInstance pool, String section) {
        String jid = IdentifyUtils.getUUID();
        JediSQL j = pool.getJedis(jid);
        Object infos = j.sendCommand(RedisCmdType.INFO, section);
        String encoding = BaseUtils.getEncoding((byte[]) infos);
        String tmp = new String((byte[]) infos, Charset.forName(encoding)).replaceAll("# " + section + "\\r\\n", "").replaceAll("\\r\\n", ";");
        pool.close(jid);
        if (SECTIONS.contains(section)) {
            return tmp.substring(0, tmp.lastIndexOf(";")).split(";");
        }
        return new String[]{};
    }

    public static Map<String, String> GetInstanceInfo(RedisPoolInstance pool, String section) {
        Map<String, String> info = new HashMap<>();
        String[] infos = GetInfoSection(pool, section);
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
