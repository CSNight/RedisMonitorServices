package csnight.redis.monitor.busi.rms;

import com.alibaba.fastjson.JSONObject;
import com.csnight.jedisql.JediSQL;
import csnight.redis.monitor.db.jpa.RmsInstance;
import csnight.redis.monitor.db.repos.RmsInsRepository;
import csnight.redis.monitor.db.repos.SysUserRepository;
import csnight.redis.monitor.exception.ConfigException;
import csnight.redis.monitor.redis.pool.MultiRedisPool;
import csnight.redis.monitor.redis.pool.PoolConfig;
import csnight.redis.monitor.redis.pool.RedisPoolInstance;
import csnight.redis.monitor.rest.rms.dto.RmsInsDto;
import csnight.redis.monitor.utils.BaseUtils;
import csnight.redis.monitor.utils.GUID;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class RmsInsManageImpl {
    @Resource
    private RmsInsRepository rmsInsRepository;
    @Resource
    private SysUserRepository userRepository;

    public List<RmsInstance> GetInstances() {
        return rmsInsRepository.findAll();
    }

    public List<RmsInstance> GetInstanceByUser(String user_id) {
        return rmsInsRepository.findByUserId(user_id);
    }

    public RmsInstance NewInstance(RmsInsDto dto) throws ConfigException {
        RmsInstance ins = new RmsInstance();
        String user_id = userRepository.findIdByUsername(BaseUtils.GetUserFromContext());
        ins.setUser_id(user_id);
        ins.setIp(dto.getIp());
        ins.setPort(dto.getPort());
        ins.setCluster_enable(false);
        ins.setInstance_name(dto.getName());
        PoolConfig config = BuildConn(dto);
        config.setUser_id(user_id);
        config.setIns_id(GUID.getUUID());
        RedisPoolInstance pool = MultiRedisPool.getInstance().addNewPool(config);
        JediSQL j = pool.getJedis(GUID.getUUID());
        String res = j.info("Server");
        return ins;
    }

    private PoolConfig BuildConn(RmsInsDto dto) throws ConfigException {
        try {
            if (dto.getIp() != null && BaseUtils.checkIp(dto.getIp())) {
                return JSONObject.parseObject(JSONObject.toJSONString(dto), PoolConfig.class);
            } else if (dto.getMaster() != null && dto.getSentinels().size() > 0) {
                return JSONObject.parseObject(JSONObject.toJSONString(dto), PoolConfig.class);
            }
        } catch (Exception ex) {
            throw new ConfigException("Redis Config Read Error" + ex.getMessage());
        }
        throw new ConfigException("Can not parse redis connection config");
    }
}
