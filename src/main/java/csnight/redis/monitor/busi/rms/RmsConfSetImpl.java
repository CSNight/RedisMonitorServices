package csnight.redis.monitor.busi.rms;

import com.alibaba.fastjson.JSONObject;
import com.csnight.jedisql.JediSQL;
import csnight.redis.monitor.db.jpa.RmsInstance;
import csnight.redis.monitor.db.repos.RmsInsRepository;
import csnight.redis.monitor.exception.ConfigException;
import csnight.redis.monitor.msg.series.RedisCmdType;
import csnight.redis.monitor.redis.pool.MultiRedisPool;
import csnight.redis.monitor.redis.pool.PoolConfig;
import csnight.redis.monitor.redis.pool.RedisPoolInstance;
import csnight.redis.monitor.rest.rms.dto.ConfigDto;
import csnight.redis.monitor.utils.BaseUtils;
import csnight.redis.monitor.utils.IdentifyUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
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
        Map<String, Object> configs = new HashMap<>();
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
        String jid = IdentifyUtils.getUUID();
        try {
            JediSQL jediSQL = pool.getJedis(jid);
            Object configList = jediSQL.sendCommand(RedisCmdType.CONFIG, "GET", "*");
            List<byte[]> configBytes = (List<byte[]>) configList;
            int len = configBytes.size();
            if (len % 2 == 0) {
                for (int i = 0; i < len; i = i + 2) {
                    byte[] value = configBytes.get(i + 1);
                    String encoding = "utf-8";
                    if (instance.getOs().toLowerCase().contains("windows") && !BaseUtils.getEncoding(value).toUpperCase().equals("UTF-8")) {
                        encoding = "gbk";
                    }
                    String val = new String(value, Charset.forName(encoding));
                    configs.put(new String(configBytes.get(i)), val);
                }
            }
        } catch (Exception ex) {
            return null;
        } finally {
            pool.close(jid);
            if (needShut) {
                MultiRedisPool.getInstance().removePool(ins_id);
            }
        }
        return configs;
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
        String jid = IdentifyUtils.getUUID();
        try {
            JediSQL jediSQL = pool.getJedis(jid);
            return jediSQL.configSet(dto.getConfKey(), dto.getConfVal());
        } finally {
            pool.close(jid);
            if (needShut) {
                MultiRedisPool.getInstance().removePool(dto.getIns_id());
            }
        }
    }

    public void GetRedisInfo() {

    }
}
