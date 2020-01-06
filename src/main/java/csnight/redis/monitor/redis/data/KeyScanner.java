package csnight.redis.monitor.redis.data;

import com.csnight.jedisql.*;
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
            Pipeline pipeline = jediSQL.pipelined();
            Map<String, Response<String>> typePipeRes = new HashMap<>();
            Map<String, Response<Long>> ttlPipeRes = new HashMap<>();

            for (String key : keys) {
                typePipeRes.put(key, pipeline.type(key));
            }
            pipeline.sync();
            for (String key : keys) {
                ttlPipeRes.put(key, pipeline.pttl(key));
            }
            pipeline.sync();
            for (String key : keys) {
                long ttl = ttlPipeRes.get(key).get();
                String t = typePipeRes.get(key).get();
                Map<String, Object> joKey = new HashMap<>();
                joKey.put("key", key);
                joKey.put("ttl", ttl);
                joKey.put("type", t);
                if (ttl == -2L || t.equals("none")) {
                    continue;
                }
                keySet.add(joKey);
            }

            pipeline.close();
            ttlPipeRes.clear();
            typePipeRes.clear();
            keys.clear();
            res.put("cursor", keysRes.getCursor());
            res.put("keys", GetKeySize(jediSQL, keySet));

        } finally {
            jediSQL.select(0);
            pool.close(jid);
            System.gc();
        }
        return res;
    }

    private List<Map<String, Object>> GetKeySize(JediSQL jediSQL, List<Map<String, Object>> keySet) {
        List<Map<String, Object>> strArr = new ArrayList<>();
        List<Map<String, Object>> listArr = new ArrayList<>();
        List<Map<String, Object>> zsetArr = new ArrayList<>();
        List<Map<String, Object>> hashArr = new ArrayList<>();
        List<Map<String, Object>> setArr = new ArrayList<>();
        for (Map<String, Object> kt : keySet) {
            switch (kt.get("type").toString()) {
                case "string":
                    strArr.add(kt);
                    break;
                case "hash":
                    hashArr.add(kt);
                    break;
                case "zset":
                    zsetArr.add(kt);
                    break;
                case "set":
                    setArr.add(kt);
                    break;
                case "list":
                    listArr.add(kt);
                    break;
            }
        }
        getStringSizes(jediSQL, strArr);
        getListSizes(jediSQL, listArr);
        getSetSizes(jediSQL, setArr);
        getZSetSizes(jediSQL, zsetArr);
        getHashSizes(jediSQL, hashArr);
        List<Map<String, Object>> res = new ArrayList<>();
        res.addAll(strArr);
        res.addAll(listArr);
        res.addAll(setArr);
        res.addAll(zsetArr);
        res.addAll(hashArr);
        strArr.clear();
        listArr.clear();
        setArr.clear();
        zsetArr.clear();
        hashArr.clear();
        System.gc();
        return res;
    }

    private void getStringSizes(JediSQL jediSQL, List<Map<String, Object>> strArr) {
        Map<String, Response<Long>> pipeRes = new HashMap<>();
        Pipeline p = jediSQL.pipelined();
        for (Map<String, Object> kt : strArr) {
            String key = kt.get("key").toString();
            pipeRes.put(key, p.strlen(key));
        }
        long s = System.currentTimeMillis();
        p.sync();
        long e = System.currentTimeMillis();
        System.out.println(e - s);
        for (Map<String, Object> kt : strArr) {
            String key = kt.get("key").toString();
            long size = pipeRes.get(key).get();
            kt.put("size", size);
        }
        p.close();
        pipeRes.clear();
        pipeRes = null;
    }

    private void getListSizes(JediSQL jediSQL, List<Map<String, Object>> listArr) {
        Map<String, Response<Long>> pipeRes = new HashMap<>();
        Pipeline p = jediSQL.pipelined();
        for (Map<String, Object> kt : listArr) {
            String key = kt.get("key").toString();
            pipeRes.put(key, p.llen(key));
        }
        p.sync();
        for (Map<String, Object> kt : listArr) {
            String key = kt.get("key").toString();
            long size = pipeRes.get(key).get();
            kt.put("size", size);
        }
        p.close();
        pipeRes.clear();
        pipeRes = null;
    }

    private void getSetSizes(JediSQL jediSQL, List<Map<String, Object>> setArr) {
        Map<String, Response<Long>> pipeRes = new HashMap<>();
        Pipeline p = jediSQL.pipelined();
        for (Map<String, Object> kt : setArr) {
            String key = kt.get("key").toString();
            pipeRes.put(key, p.scard(key));
        }
        p.sync();
        for (Map<String, Object> kt : setArr) {
            String key = kt.get("key").toString();
            long size = pipeRes.get(key).get();
            kt.put("size", size);
        }
        p.close();
        pipeRes.clear();
        pipeRes = null;
    }

    private void getZSetSizes(JediSQL jediSQL, List<Map<String, Object>> zsetArr) {
        Map<String, Response<Long>> pipeRes = new HashMap<>();
        Pipeline p = jediSQL.pipelined();
        for (Map<String, Object> kt : zsetArr) {
            String key = kt.get("key").toString();
            pipeRes.put(key, p.zcount(key, "-inf", "+inf"));
        }
        p.sync();
        for (Map<String, Object> kt : zsetArr) {
            String key = kt.get("key").toString();
            long size = pipeRes.get(key).get();
            kt.put("size", size);
        }
        p.close();
        pipeRes.clear();
        pipeRes = null;
    }

    private void getHashSizes(JediSQL jediSQL, List<Map<String, Object>> hashArr) {
        Map<String, Response<Long>> pipeRes = new HashMap<>();
        Pipeline p = jediSQL.pipelined();
        for (Map<String, Object> kt : hashArr) {
            String key = kt.get("key").toString();
            pipeRes.put(key, p.hlen(key));
        }
        p.sync();
        for (Map<String, Object> kt : hashArr) {
            String key = kt.get("key").toString();
            long size = pipeRes.get(key).get();
            kt.put("size", size);
        }
        p.close();
        pipeRes.clear();
        pipeRes = null;
    }

    private void GetKeyValue(JediSQL jediSQL, Map<String, Object> kt) {
        String key = kt.get("key").toString();
        switch (kt.get("type").toString()) {
            case "string":
                String kv = jediSQL.get(key);
                kt.put("val", kv);
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
