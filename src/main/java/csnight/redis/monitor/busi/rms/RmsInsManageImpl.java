package csnight.redis.monitor.busi.rms;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.db.jpa.RmsInstance;
import csnight.redis.monitor.db.repos.RmsInsRepository;
import csnight.redis.monitor.db.repos.SysUserRepository;
import csnight.redis.monitor.exception.ConfigException;
import csnight.redis.monitor.redis.pool.MultiRedisPool;
import csnight.redis.monitor.redis.pool.PoolConfig;
import csnight.redis.monitor.redis.pool.RedisPoolInstance;
import csnight.redis.monitor.redis.statistic.InfoCmdParser;
import csnight.redis.monitor.rest.rms.dto.RmsInsDto;
import csnight.redis.monitor.utils.BaseUtils;
import csnight.redis.monitor.utils.GUID;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RmsInsManageImpl {
    @Resource
    private RmsInsRepository rmsInsRepository;
    @Resource
    private SysUserRepository userRepository;

    @Cacheable(value = "instances")
    public List<RmsInstance> GetInstances() {
        return rmsInsRepository.findAll();
    }

    @Cacheable(value = "instance", key = "#user_id")
    public List<RmsInstance> GetInstanceByUser(String user_id) {
        return rmsInsRepository.findByUserId(user_id);
    }

    @Caching(evict = {@CacheEvict(value = "instances", beforeInvocation = true, allEntries = true),
            @CacheEvict(value = "instance", key = "#result.user_id")})
    public RmsInstance NewInstance(RmsInsDto dto) throws ConfigException {
        RmsInstance ins = new RmsInstance();
        String user_id = userRepository.findIdByUsername(BaseUtils.GetUserFromContext());
        ins.setId(GUID.getUUID());
        ins.setUser_id(user_id);
        ins.setIp(dto.getIp());
        ins.setPort(dto.getPort());
        ins.setInstance_name(dto.getName());
        ins.setCreate_time(new Date());
        PoolConfig config = BuildConfig(dto);
        config.setUser_id(user_id);
        config.setIns_id(ins.getId());
        RedisPoolInstance pool = MultiRedisPool.getInstance().addNewPool(config);
        if (pool != null) {
            ins.setState(true);
            return parseInfo(ins, pool, config);
        } else {
            throw new ConfigException("Can not build a redis connection pool");
        }
    }

    @CacheEvict(value = {"instances", "instance"}, beforeInvocation = true, allEntries = true)
    public String DeleteInstance(String ins_id) {
        try {
            boolean res = MultiRedisPool.getInstance().removePool(ins_id);
            rmsInsRepository.deleteById(ins_id);
            return "success";
        } catch (Exception ex) {
            return "failed";
        }
    }

    @Caching(evict = {@CacheEvict(value = "instances", beforeInvocation = true, allEntries = true),
            @CacheEvict(value = "instance", key = "#result.user_id")})
    public RmsInstance ModifyInsName(RmsInsDto dto) {
        Optional<RmsInstance> rms = rmsInsRepository.findById(dto.getId());
        if (rms.isPresent()) {
            RmsInstance oldIns = rms.get();
            oldIns.setInstance_name(dto.getName());
            return rmsInsRepository.save(oldIns);
        }
        return null;
    }

    @Caching(evict = {@CacheEvict(value = "instances", beforeInvocation = true, allEntries = true),
            @CacheEvict(value = "instance", key = "#result.user_id")})
    public RmsInstance ModifyInsState(RmsInsDto dto) throws ConfigException {
        Optional<RmsInstance> rms = rmsInsRepository.findById(dto.getId());
        if (rms.isPresent()) {
            RmsInstance oldIns = rms.get();
            if (oldIns.isState() && MultiRedisPool.getInstance().getPool(oldIns.getId()) != null && !dto.isState()) {
                //TODO 停止关联定时任务
                boolean isShutdown = MultiRedisPool.getInstance().removePool(oldIns.getId());
                if (isShutdown) {
                    oldIns.setState(false);
                    return rmsInsRepository.save(oldIns);
                }
            } else {
                PoolConfig config = JSONObject.parseObject(oldIns.getConn(), PoolConfig.class);
                RedisPoolInstance pool = MultiRedisPool.getInstance().addNewPool(config);
                if (pool != null) {
                    oldIns.setState(true);
                    return rmsInsRepository.save(oldIns);
                }
            }
            return oldIns;
        }
        return null;
    }

    @Caching(evict = {@CacheEvict(value = "instances", beforeInvocation = true, allEntries = true),
            @CacheEvict(value = "instance", key = "#result.user_id")})
    public RmsInstance ModifyInsConn(RmsInsDto dto) throws ConfigException {
        Optional<RmsInstance> rms = rmsInsRepository.findById(dto.getId());
        if (rms.isPresent()) {
            RmsInstance oldIns = rms.get();
            PoolConfig config_old = JSONObject.parseObject(oldIns.getConn(), PoolConfig.class);
            PoolConfig config_new = BuildConfig(dto);
            config_new.setIns_id(oldIns.getId());
            config_new.setUser_id(oldIns.getUser_id());
            config_old.checkMd5();
            config_new.checkMd5();
            if (!config_old.getUin().equals(config_new.getUin())) {
                if (oldIns.isState() && MultiRedisPool.getInstance().getPool(oldIns.getId()) != null) {
                    //TODO 停止关联定时任务
                    boolean isShutdown = MultiRedisPool.getInstance().removePool(oldIns.getId());
                    if (isShutdown) {
                        oldIns.setState(false);
                        oldIns = rmsInsRepository.save(oldIns);
                    }
                }
                RedisPoolInstance pool = MultiRedisPool.getInstance().addNewPool(config_new);
                if (pool != null) {
                    oldIns.setState(true);
                    return parseInfo(oldIns, pool, config_new);
                } else {
                    throw new ConfigException("Can not build a redis connection pool");
                }
            } else {
                throw new ConfigException("Redis instance conflict");
            }
        }
        return null;
    }

    @Caching(evict = {@CacheEvict(value = "instances", beforeInvocation = true, allEntries = true),
            @CacheEvict(value = "instance", key = "#result.user_id")})
    public RmsInstance UpdateInsMeta(RmsInsDto dto) throws ConfigException {
        Optional<RmsInstance> rms = rmsInsRepository.findById(dto.getId());
        if (rms.isPresent()) {
            RmsInstance oldIns = rms.get();
            PoolConfig config = JSONObject.parseObject(oldIns.getConn(), PoolConfig.class);
            RedisPoolInstance pool = MultiRedisPool.getInstance().getPool(oldIns.getId());
            if (pool != null) {
                return parseInfo(oldIns, pool, config);
            } else {
                RedisPoolInstance poolTemp = MultiRedisPool.getInstance().addNewPool(config);
                if (poolTemp != null) {
                    oldIns.setState(true);
                    return parseInfo(oldIns, poolTemp, config);
                } else {
                    return oldIns;
                }
            }
        }
        return null;
    }

    private RmsInstance parseInfo(RmsInstance ins, RedisPoolInstance pool, PoolConfig config) {
        try {
            Map<String, String> info = InfoCmdParser.GetInstanceInfo(pool, "Server");
            ins.setArch_bits(Integer.parseInt(info.get("arch_bits")));
            ins.setOs(info.get("os"));
            ins.setMode(info.get("redis_mode"));
            ins.setProc_id(Integer.parseInt(info.get("process_id")));
            ins.setUptime_in_seconds(Integer.parseInt(info.get("uptime_in_seconds")));
            ins.setHz(Integer.parseInt(info.get("hz")));
            ins.setExec(info.get("executable"));
            ins.setConfig(info.get("config_file"));
            ins.setVersion(info.get("redis_version"));
            ins.setRole(InfoCmdParser.GetInfoBySectionKey(pool, "Replication", "role"));
            ins.setConn(JSONObject.toJSONString(config));
            return rmsInsRepository.save(ins);
        } catch (Exception ex) {
            MultiRedisPool.getInstance().removePool(ins.getId());
            return null;
        }
    }

    private PoolConfig BuildConfig(RmsInsDto dto) throws ConfigException {
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
