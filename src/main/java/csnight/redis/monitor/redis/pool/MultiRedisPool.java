package csnight.redis.monitor.redis.pool;

import csnight.redis.monitor.busi.rms.RmsInsManageImpl;
import csnight.redis.monitor.exception.ConfigException;
import csnight.redis.monitor.utils.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MultiRedisPool {
    private static Logger _log = LoggerFactory.getLogger(MultiRedisPool.class);
    private static MultiRedisPool ourInstance;
    private Map<String, RedisPoolInstance> ConnPools = new ConcurrentHashMap<>();
    private Map<String, List<String>> UserPools = new ConcurrentHashMap<>();
    private List<String> md5s = new ArrayList<>();

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

    public RedisPoolInstance addNewPool(PoolConfig poolConfig) throws ConfigException {
        try {
            RedisPoolInstance ins = new RedisPoolInstance(poolConfig);
            if (ins.getUin().equals("") || md5s.indexOf(ins.getUin()) != -1) {
                throw new ConfigException("Can not add a same redis instance again");
            }
            ins.BuildJedisPool();
            md5s.add(ins.getUin());
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
            throw new ConfigException(e.getMessage());
        }
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
                md5s.remove(pool.getUin());
                UserPools.get(user_id).remove(id);
                if (UserPools.get(user_id).size() == 0) {
                    UserPools.remove(user_id);
                }
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

    public boolean shutdownUserPools(String user_id) {
        List<String> instances = UserPools.get(user_id);
        try {
            for (String ins_id : instances) {
                RedisPoolInstance pool = ConnPools.get(ins_id);
                if (pool != null) {
                    pool.shutdown();
                    md5s.remove(pool.getUin());
                    ConnPools.remove(ins_id);
                }
            }
            UserPools.remove(user_id);
            System.gc();
            return true;
        } catch (Exception ex) {
            _log.error(ex.getMessage());
            return false;
        }
    }

    public void shutdown() {
        for (RedisPoolInstance pool : ConnPools.values()) {
            pool.shutdown();
        }
        ReflectUtils.getBean(RmsInsManageImpl.class).ChangeAllState(ConnPools.keySet());
    }

}
