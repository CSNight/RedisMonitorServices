package csnight.redis.monitor.redis.pool;

import com.csnight.jedisql.HostAndPort;
import com.csnight.jedisql.JediSQL;
import com.csnight.jedisql.JedisPool;
import com.csnight.jedisql.JedisSentinelPool;
import com.csnight.jedisql.exceptions.JedisConnectionException;
import csnight.redis.monitor.exception.ValidateException;

import java.util.HashMap;
import java.util.Map;

public class RedisPoolInstance {
    private JedisPool jedisPool = null;
    private JedisSentinelPool jedisSenPool = null;
    private Map<String, JediSQL> rcs = new HashMap<>();
    private PoolConfig config;
    private String id = "";
    private String user_id = "";
    private String uin = "";

    public RedisPoolInstance(PoolConfig config) {
        this.config = config;
        id = config.getIns_id();
        user_id = config.getUser_id();
        config.checkMd5();
        this.uin = config.getUin();
    }

    public String getUin() {
        return uin;
    }

    public Map<String, JediSQL> getRcs() {
        return rcs;
    }

    public String getId() {
        return id;
    }

    public String getUser_id() {
        return user_id;
    }

    public HostAndPort getHP() {
        if (config.getPoolType().equals("sen")) {
            return jedisSenPool.getCurrentHostMaster();
        } else {
            return new HostAndPort(config.getIp(), config.getPort());
        }
    }

    void BuildJedisPool() throws ValidateException {
        if (config.getPoolType().equals("sen") && config.checkSentinelsConfig()) {
            jedisSenPool = new JedisSentinelPool(config.getMaster(), config.getSentinels(),
                    config.BuildJedisConfig(), config.getTimeOut(), config.getPassword(), config.getDb());
            //connect test;
            JediSQL j = jedisSenPool.getResource();
            j.close();
        } else if (config.getPoolType().equals("sin")) {
            jedisPool = new JedisPool(config.BuildJedisConfig(), config.getIp(), config.getPort(),
                    config.getTimeOut(), config.getPassword(), config.getDb());
            //connect test;
            JediSQL j = jedisPool.getResource();
            j.close();
        } else {
            throw new ValidateException("Redis pool build failed due to config");
        }
    }


    /**
     * 获取Jedis实例
     *
     * @return Jedis
     */
    public synchronized JediSQL getJedis(String guid) {
        try {
            if (config.getPoolType().equals("sen")) {
                if (jedisSenPool != null) {
                    JediSQL jedis = jedisSenPool.getResource();
                    rcs.put(guid, jedis);
                    return jedis;
                } else {
                    return null;
                }
            } else {
                if (jedisPool != null) {
                    JediSQL jedis = jedisPool.getResource();
                    rcs.put(guid, jedis);
                    return jedis;
                } else {
                    return null;
                }
            }
        } catch (Exception e) {
            throw new JedisConnectionException(e);
        }
    }

    /**
     * 释放jedis资源
     *
     * @param guid 唯一id
     */
    public void close(String guid) {
        if (rcs.containsKey(guid)) {
            rcs.get(guid).close();
            rcs.remove(guid);
        }
    }

    public void shutdown() {
        if (config.getPoolType().equals("sen")) {
            if (jedisSenPool != null) {
                rcs.forEach((key, jedis) -> jedis.close());
                jedisSenPool.close();
            }
        } else {
            if (jedisPool != null) {
                rcs.forEach((key, jedis) -> jedis.close());
                jedisPool.close();
            }
        }
    }
}
