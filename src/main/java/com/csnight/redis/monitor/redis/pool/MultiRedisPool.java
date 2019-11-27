package com.csnight.redis.monitor.redis.pool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MultiRedisPool {
    private static Logger _log = LoggerFactory.getLogger(MultiRedisPool.class);
    private static MultiRedisPool ourInstance;
    private Map<String, RedisPoolInstance> ConnPools = new ConcurrentHashMap<>();

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

    public RedisPoolInstance addNewPool(PoolConfig poolConfig) {
        RedisPoolInstance ins = new RedisPoolInstance(poolConfig);
        ins.BuildJedisPool();
        ConnPools.put(ins.getId(), ins);
        return ins;
    }

    public boolean removePool(String id) {
        try {
            if (ConnPools.containsKey(id)) {
                ConnPools.get(id).shutdown();
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
