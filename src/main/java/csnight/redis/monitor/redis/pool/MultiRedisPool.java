package csnight.redis.monitor.redis.pool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MultiRedisPool {
    private static Logger _log = LoggerFactory.getLogger(MultiRedisPool.class);
    private static MultiRedisPool ourInstance;
    private Map<String, RedisPoolInstance> ConnPools = new ConcurrentHashMap<>();
    private Map<String, List<String>> UserPools = new ConcurrentHashMap<>();

    public static MultiRedisPool getInstance() {
        if (ourInstance == null) {
            synchronized (MultiRedisPool.class) {
                if (ourInstance == null) {
                    ourInstance = new MultiRedisPool();
                }
            }
        }
        return ourInstance;
    }

    public Map<String, RedisPoolInstance> getConnPools() {
        return ConnPools;
    }

    public Map<String, List<String>> getUserPools() {
        return UserPools;
    }

    public RedisPoolInstance addNewPool(PoolConfig poolConfig) {
        try {
            RedisPoolInstance ins = new RedisPoolInstance(poolConfig);
            ins.BuildJedisPool();
            ConnPools.put(ins.getId(), ins);
            if (UserPools.containsKey(ins.getUser_id())) {
                UserPools.get(ins.getUser_id()).add(ins.getId());
            } else {
                List<String> inses = new ArrayList<>();
                inses.add(ins.getId());
                UserPools.put(ins.getUser_id(), inses);
            }
            return ins;
        } catch (Exception e) {
            _log.error(e.getMessage());
        }
        return null;
    }

    public RedisPoolInstance getPool(String ins) {
        return ConnPools.get(ins);
    }

    public boolean removePool(String id) {
        try {
            RedisPoolInstance pool = ConnPools.get(id);
            if (pool != null) {
                pool.shutdown();
                String user_id = pool.getUser_id();
                UserPools.get(user_id).remove(id);
                ConnPools.remove(id);
                System.gc();
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            _log.error(ex.getMessage());
            return false;
        }
    }

}
