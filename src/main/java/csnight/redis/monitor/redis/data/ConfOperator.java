package csnight.redis.monitor.redis.data;

import com.csnight.jedisql.JediSQL;
import csnight.redis.monitor.db.jpa.RmsInstance;
import csnight.redis.monitor.msg.series.RedisCmdType;
import csnight.redis.monitor.redis.pool.RedisPoolInstance;
import csnight.redis.monitor.rest.rms.dto.ConfigDto;
import csnight.redis.monitor.utils.BaseUtils;
import csnight.redis.monitor.utils.IdentifyUtils;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author csnight
 * @description
 * @since 2020/1/14 15:26
 */
public class ConfOperator {

    public Map<String, Object> getConfig(RedisPoolInstance pool, RmsInstance ins) {
        Map<String, Object> configs = new HashMap<>();
        String jid = IdentifyUtils.getUUID();
        try {
            JediSQL jediSQL = pool.getJedis(jid);
            Object configList = jediSQL.sendCommand(RedisCmdType.CONFIG, "GET", "*");
            List<byte[]> configBytes = (List<byte[]>) configList;
            int len = configBytes.size();
            if (len % 2 == 0) {
                for (int i = 0; i < len; i = i + 2) {
                    byte[] value = configBytes.get(i + 1);
                    String encoding = "utf-8";
                    if (ins.getOs().toLowerCase().contains("windows") && !BaseUtils.getEncoding(value).toUpperCase().equals("UTF-8")) {
                        encoding = "gbk";
                    }
                    String val = new String(value, Charset.forName(encoding));
                    configs.put(new String(configBytes.get(i)), val);
                }
            }
        } catch (Exception ex) {
            return null;
        } finally {
            pool.close(jid);
        }
        return configs;
    }

    public String saveConfig(RedisPoolInstance pool, ConfigDto dto) {
        String jid = IdentifyUtils.getUUID();
        try {
            JediSQL jediSQL = pool.getJedis(jid);
            return jediSQL.configSet(dto.getConfKey(), dto.getConfVal());
        } finally {
            pool.close(jid);
        }
    }
}
