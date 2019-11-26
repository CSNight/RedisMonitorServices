package com.csnight.redis.monitor.redis.pool;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MultiRedisPool {
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

}
