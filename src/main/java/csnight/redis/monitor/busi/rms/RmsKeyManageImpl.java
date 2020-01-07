package csnight.redis.monitor.busi.rms;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.db.jpa.RmsInstance;
import csnight.redis.monitor.db.repos.RmsInsRepository;
import csnight.redis.monitor.exception.ConfigException;
import csnight.redis.monitor.redis.data.KeyOperator;
import csnight.redis.monitor.redis.pool.MultiRedisPool;
import csnight.redis.monitor.redis.pool.PoolConfig;
import csnight.redis.monitor.redis.pool.RedisPoolInstance;
import csnight.redis.monitor.rest.rms.dto.KeyEntDto;
import csnight.redis.monitor.rest.rms.dto.KeyScanDto;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Service
public class RmsKeyManageImpl {
    @Resource
    private RmsInsRepository rmsInsRepository;
    private KeyOperator keyOperator = new KeyOperator();

    public Map<String, Object> GetDBKeys(KeyScanDto dto) throws ConfigException {
        RmsInstance instance = rmsInsRepository.findOnly(dto.getIns_id());
        if (instance == null || !instance.getRole().equals("master")) {
            return null;
        }
        RedisPoolInstance pool = MultiRedisPool.getInstance().getPool(instance.getId());
        if (pool == null) {
            PoolConfig config = JSONObject.parseObject(instance.getConn(), PoolConfig.class);
            pool = MultiRedisPool.getInstance().addNewPool(config);
            if (pool != null) {
                instance.setState(true);
                rmsInsRepository.save(instance);
            } else {
                return null;
            }
        }
        return keyOperator.ScanKeys(pool, dto);
    }

    public Map<String, Object> GetDBKeyValue(KeyEntDto dto) {
        RmsInstance instance = rmsInsRepository.findOnly(dto.getIns_id());
        if (instance == null || !instance.getRole().equals("master")) {
            return null;
        }
        RedisPoolInstance pool = MultiRedisPool.getInstance().getPool(instance.getId());
        if (pool == null) {
            return null;
        }
        return keyOperator.GetKeyValue(pool, dto);
    }

    public String DeleteKeys(KeyEntDto dto) {
        RmsInstance instance = rmsInsRepository.findOnly(dto.getIns_id());
        if (instance == null || !instance.getRole().equals("master")) {
            return "failed";
        }
        RedisPoolInstance pool = MultiRedisPool.getInstance().getPool(instance.getId());
        if (pool == null) {
            return "failed";
        }
        if (dto.getKeys().size() == 0) {
            return "success";
        }
        return keyOperator.DeleteKey(pool, dto) ? "success" : "failed";
    }
}
