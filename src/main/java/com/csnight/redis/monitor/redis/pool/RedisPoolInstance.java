package com.csnight.redis.monitor.redis.pool;

import com.csnight.jedisql.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class RedisPoolInstance {
    private String serverIp = "";
    private int port = 6379;
    private String st_pool_type = "sin";
    private String password = null;
    private JedisPool jedisPool = null;
    private JedisSentinelPool jedisSenPool = null;
    private int database = 0;
    private HashMap<String, JediSQL> redisClient = new HashMap<>();

    public RedisPoolInstance() {
    }


    public String getSt_pool_type() {
        return st_pool_type;
    }

    public void setSt_pool_type(String st_pool_type) {
        this.st_pool_type = st_pool_type;
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public JedisSentinelPool getJedisSenPool() {
        return jedisSenPool;
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public HashMap<String, JediSQL> getRedisClient() {
        return redisClient;
    }

    public void setRedisClient(HashMap<String, JediSQL> redisClient) {
        this.redisClient = redisClient;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public HostAndPort getHP() {
        if (st_pool_type.equals("sen")) {
            return jedisSenPool.getCurrentHostMaster();
        } else {
            return new HostAndPort(serverIp, port);
        }
    }

    public void BuildJedisPool() {
        if (st_pool_type.equals("sen")) {
            Set<String> sentinels = new HashSet<>();
            String master = "";
            jedisSenPool = new JedisSentinelPool(master, sentinels, BuildJedisConfig(), 0);
        } else {
            jedisPool = new JedisPool(BuildJedisConfig(), serverIp, port, 3000, password, database);
        }
    }

    private JedisPoolConfig BuildJedisConfig() {
        JedisPoolConfig config = new JedisPoolConfig();
        // 连接耗尽时是否阻塞, false报异常,true阻塞直到超时, 默认true
        config.setBlockWhenExhausted(true);
        // 设置的逐出策略类名, 默认DefaultEvictionPolicy(当连接超过最大空闲时间,或连接数超过最大空闲连接数)
        config.setEvictionPolicyClassName("org.apache.commons.pool2.impl.DefaultEvictionPolicy");
        // 是否启用pool的jmx管理功能, 默认true
        config.setJmxEnabled(true);
        // 最大连接数, 默认8个
        config.setMaxTotal(2000);
        // 表示当borrow(引入)一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；
        config.setMaxWaitMillis(1000 * 100);
        // 在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
        config.setTestOnBorrow(true);
        return config;
    }

    /**
     * 获取Jedis实例
     *
     * @return Jedis
     */
    public synchronized JediSQL getJedis(String guid) {
        try {
            if (st_pool_type.equals("sen")) {
                if (jedisSenPool != null) {
                    JediSQL jedis = jedisSenPool.getResource();
                    redisClient.put(guid, jedis);
                    return jedis;
                } else {
                    return null;
                }
            } else {
                if (jedisPool != null) {
                    JediSQL jedis = jedisPool.getResource();
                    redisClient.put(guid, jedis);
                    return jedis;
                } else {
                    return null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 释放jedis资源
     *
     * @param guid 唯一id
     */
    public void close(String guid) {
        if (redisClient.containsKey(guid)) {
            redisClient.get(guid).close();
            redisClient.remove(guid);
        }
    }

    public void shutdown() {
        if (st_pool_type.equals("sen")) {
            if (jedisSenPool != null) {
                redisClient.forEach((key, jedis) -> jedis.close());
                jedisSenPool.close();
                jedisSenPool.destroy();
            }
        } else {
            if (jedisPool != null) {
                redisClient.forEach((key, jedis) -> jedis.close());
                jedisPool.close();
                jedisPool.destroy();
            }
        }

    }
}
