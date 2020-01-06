package csnight.redis.monitor.redis.data;

import com.csnight.jedisql.JediSQL;
import com.csnight.jedisql.ScanParams;
import com.csnight.jedisql.ScanResult;
import com.csnight.jedisql.Tuple;
import csnight.redis.monitor.redis.pool.RedisPoolInstance;
import csnight.redis.monitor.rest.rms.dto.KeyScanDto;
import csnight.redis.monitor.utils.IdentifyUtils;

import java.util.*;

/**
 * @author csnight
 * @description
 * @since 2020/1/6 09:47
 */
public class KeyScanner {
    public Map<String, Object> ScanKeys(RedisPoolInstance pool, KeyScanDto dto) {
        Map<String, Object> res = new HashMap<>();
        List<Map<String, Object>> keySet = new ArrayList<>();
        String jid = IdentifyUtils.getUUID();
        JediSQL jediSQL = pool.getJedis(jid);
        jediSQL.select(dto.getDb());
        ScanParams params = new ScanParams();
        params.match(dto.getMatch());
        params.count(dto.getCount());
        try {
            ScanResult<String> keysRes = jediSQL.scan(dto.getCursor(), params);
            keysRes.getCursor();
            List<String> keys = keysRes.getResult();
            for (String key : keys) {
                long ttl = jediSQL.pttl(key);
                String t = jediSQL.type(key);
                if (ttl == -2L || t.equals("none")) {
                    continue;
                }
                Map<String, Object> joKey = new HashMap<>();
                joKey.put("key", key);
                joKey.put("ttl", ttl);
                joKey.put("type", t);
                keySet.add(joKey);
            }
            keys.clear();
            res.put("cursor", keysRes.getCursor());
            GetKeySize(jediSQL,keySet);
            res.put("keys", keySet);
        } finally {
            jediSQL.select(0);
            pool.close(jid);
        }
        return res;
    }

    private void GetKeySize(JediSQL jediSQL, List<Map<String, Object>> keySet) {
        for (Map<String, Object> kt : keySet) {
            String key = kt.get("key").toString();
            switch (kt.get("type").toString()) {
                case "string":
                    String kv = jediSQL.get(key);
                    kt.put("size", kv.length());
                    break;
                case "hash":
                    long hs = jediSQL.hlen(key);
                    kt.put("size", hs);
                    break;
                case "zset":
                    long zs = jediSQL.zcount(key, "-inf", "+inf");
                    kt.put("size", zs);
                    break;
                case "set":
                    long ss = jediSQL.scard(key);
                    kt.put("size", ss);
                    break;
                case "list":
                    long ls = jediSQL.llen(key);
                    kt.put("size", ls);
                    break;
            }
        }
    }

    private void GetKeyValue(JediSQL jediSQL, Map<String, Object> kt) {
        String key = kt.get("key").toString();
        switch (kt.get("type").toString()) {
            case "string":
                String kv = jediSQL.get(key);
                kt.put("val", kv);
                if (kv.startsWith("HYLL")) {
                    kt.put("type", "hyperLogLog");
                }
                break;
            case "hash":
                Map<String, String> table = jediSQL.hgetAll(key);
                kt.put("val", table);
                break;
            case "zset":
                Set<Tuple> zset = jediSQL.zrangeByScoreWithScores(key, "-inf", "+inf");
                kt.put("val", zset);
                break;
            case "set":
                Set<String> set = jediSQL.smembers(key);
                kt.put("val", set);
                break;
            case "list":
                List<String> list = jediSQL.lrange(key, 0, -1);
                kt.put("val", list);
                break;
        }
    }
}
