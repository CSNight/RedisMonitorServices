package csnight.redis.monitor.busi.rms;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.db.jpa.RmsInstance;
import csnight.redis.monitor.db.repos.RmsInsRepository;
import csnight.redis.monitor.exception.ConfigException;
import csnight.redis.monitor.redis.data.ConfOperator;
import csnight.redis.monitor.redis.pool.MultiRedisPool;
import csnight.redis.monitor.redis.pool.PoolConfig;
import csnight.redis.monitor.redis.pool.RedisPoolInstance;
import csnight.redis.monitor.redis.statistic.InfoCmdParser;
import csnight.redis.monitor.rest.rms.dto.ConfigDto;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author csnight
 * @description Redis config get set
 * @since 2020-1-12 11:43
 */
@Service
public class RmsConfSetImpl {
    @Resource
    private RmsInsRepository rmsInsRepository;

    public Map<String, Object> GetConfig(String ins_id) throws ConfigException {
        RmsInstance instance = rmsInsRepository.findOnly(ins_id);
        if (instance == null || instance.getRole().equals("sentinel")) {
            return null;
        }
        RedisPoolInstance pool = MultiRedisPool.getInstance().getPool(instance.getId());
        boolean needShut = false;
        if (pool == null) {
            PoolConfig config = JSONObject.parseObject(instance.getConn(), PoolConfig.class);
            pool = MultiRedisPool.getInstance().addNewPool(config);
            if (pool != null) {
                needShut = true;
            } else {
                return null;
            }
        }
        try {
            ConfOperator confOperator = new ConfOperator();
            return confOperator.getConfig(pool, instance);
        } catch (Exception ex) {
            return null;
        } finally {
            if (needShut) {
                MultiRedisPool.getInstance().removePool(ins_id);
            }
        }
    }

    public String SaveConfig(ConfigDto dto) throws ConfigException {
        RmsInstance instance = rmsInsRepository.findOnly(dto.getIns_id());
        if (instance == null || instance.getRole().equals("sentinel")) {
            return null;
        }
        RedisPoolInstance pool = MultiRedisPool.getInstance().getPool(instance.getId());
        boolean needShut = false;
        if (pool == null) {
            PoolConfig config = JSONObject.parseObject(instance.getConn(), PoolConfig.class);
            pool = MultiRedisPool.getInstance().addNewPool(config);
            if (pool != null) {
                needShut = true;
            } else {
                return null;
            }
        }
        try {
            ConfOperator confOperator = new ConfOperator();
            return confOperator.saveConfig(pool, dto);
        } finally {
            if (needShut) {
                MultiRedisPool.getInstance().removePool(dto.getIns_id());
            }
        }
    }

    public Map<String, Map<String, String>> GetRedisInfo(String ins, String isMonitor) throws ConfigException {
        RmsInstance instance = rmsInsRepository.findOnly(ins);
        if (instance == null ) {
            return null;
        }
        RedisPoolInstance pool = MultiRedisPool.getInstance().getPool(instance.getId());
        boolean needShut = false;
        if (pool == null) {
            PoolConfig config = JSONObject.parseObject(instance.getConn(), PoolConfig.class);
            pool = MultiRedisPool.getInstance().addNewPool(config);
            if (pool != null) {
                needShut = true;
                if (isMonitor.equals("true")) {
                    instance.setState(true);
                    rmsInsRepository.save(instance);
                    needShut = false;
                }
            } else {
                return null;
            }
        }
        try {
            return InfoCmdParser.GetInfoAll(pool);
        } finally {
            if (needShut && isMonitor.equals("false")) {
                boolean isShut = MultiRedisPool.getInstance().removePool(ins);
                instance.setState(!isShut);
                rmsInsRepository.save(instance);
            }
        }
    }
}
