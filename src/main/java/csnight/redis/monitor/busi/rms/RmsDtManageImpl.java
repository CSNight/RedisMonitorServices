package csnight.redis.monitor.busi.rms;

import com.alibaba.fastjson.JSONObject;
import com.csnight.jedisql.JediSQL;
import csnight.redis.monitor.db.jpa.RmsInstance;
import csnight.redis.monitor.db.repos.RmsInsRepository;
import csnight.redis.monitor.exception.ConfigException;
import csnight.redis.monitor.redis.pool.MultiRedisPool;
import csnight.redis.monitor.redis.pool.PoolConfig;
import csnight.redis.monitor.redis.pool.RedisPoolInstance;
import csnight.redis.monitor.utils.IdentifyUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author csnight
 * @description
 * @since 2019/12/31 13:50
 */
@Service
public class RmsDtManageImpl {
    @Resource
    private RmsInsRepository rmsInsRepository;

    public List<JSONObject> GetDatabases() throws ConfigException {
        List<JSONObject> res = new ArrayList<>();
        List<RmsInstance> instances = rmsInsRepository.findAll(Sort.by(Sort.Direction.ASC, "ct"));
        for (RmsInstance instance : instances) {
            List<JSONObject> dbs = InstanceDBCount(instance,false);
            JSONObject JoIns = JSONObject.parseObject(JSONObject.toJSONString(instance));
            JoIns.put("children", dbs);
            JoIns.put("label", instance.getInstance_name());
            JoIns.put("type", "ins");
            JoIns.put("dbCount", dbs.size());
            res.add(JoIns);
        }
        return res;
    }

    public List<JSONObject> GetDatabaseByUser(String user_id) throws ConfigException {
        List<JSONObject> res = new ArrayList<>();
        List<RmsInstance> instances = rmsInsRepository.findByUserId(user_id);
        for (RmsInstance instance : instances) {
            List<JSONObject> dbs = InstanceDBCount(instance,false);
            JSONObject JoIns = JSONObject.parseObject(JSONObject.toJSONString(instance));
            JoIns.put("children", dbs);
            JoIns.put("label", instance.getInstance_name());
            JoIns.put("type", "ins");
            JoIns.put("dbCount", dbs.size());
            res.add(JoIns);
        }
        return res;
    }

    public JSONObject GetDatabaseById(String ins_id) throws ConfigException {
        RmsInstance instance = rmsInsRepository.findOnly(ins_id);
        if (instance != null) {
            List<JSONObject> dbs = InstanceDBCount(instance, true);
            JSONObject JoIns = JSONObject.parseObject(JSONObject.toJSONString(instance));
            JoIns.put("children", dbs);
            JoIns.put("label", instance.getInstance_name());
            JoIns.put("type", "ins");
            JoIns.put("dbCount", dbs.size());
            return JoIns;
        }
        return null;
    }

    private List<JSONObject> InstanceDBCount(RmsInstance instance, boolean tryConnect) throws ConfigException {
        RedisPoolInstance pool = MultiRedisPool.getInstance().getPool(instance.getId());
        List<JSONObject> dbs = new ArrayList<>();
        boolean needShut = false;
        if (pool == null && !tryConnect) {
            return dbs;
        } else if (pool == null && tryConnect) {
            PoolConfig config = JSONObject.parseObject(instance.getConn(), PoolConfig.class);
            pool = MultiRedisPool.getInstance().addNewPool(config);
            if (pool != null) {
                needShut = true;
            } else {
                return dbs;
            }
        }
        String jid = IdentifyUtils.getUUID();
        JediSQL jediSQL = pool.getJedis(jid);
        List<String> res = jediSQL.configGet("databases");
        if (res != null && res.size() == 2) {
            int dbCount = Integer.parseInt(res.get(1));
            for (int i = 0; i < dbCount; i++) {
                jediSQL.select(i);
                long keyCount = jediSQL.dbSize();
                JSONObject joDb = new JSONObject();
                joDb.put("id", "");
                joDb.put("keySize", keyCount);
                joDb.put("type", "db");
                joDb.put("label", "db" + i);
                joDb.put("children", new ArrayList<>());
                joDb.put("ins", instance.getId());
                dbs.add(joDb);
            }
        }
        jediSQL.select(0);
        pool.close(jid);
        if (needShut) {
            MultiRedisPool.getInstance().removePool(pool.getId());
        }
        return dbs;
    }
}
