package csnight.redis.monitor.busi.rms;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.busi.rms.exp.InsQueryExp;
import csnight.redis.monitor.busi.sys.exp.UserQueryExp;
import csnight.redis.monitor.db.blurry.QueryAnnotationProcess;
import csnight.redis.monitor.db.jpa.RmsInstance;
import csnight.redis.monitor.db.jpa.SysRole;
import csnight.redis.monitor.db.jpa.SysUser;
import csnight.redis.monitor.db.repos.RmsInsRepository;
import csnight.redis.monitor.db.repos.SysUserRepository;
import csnight.redis.monitor.exception.ConfigException;
import csnight.redis.monitor.exception.ConflictsException;
import csnight.redis.monitor.msg.MsgBus;
import csnight.redis.monitor.redis.pool.MultiRedisPool;
import csnight.redis.monitor.redis.pool.PoolConfig;
import csnight.redis.monitor.redis.pool.RedisPoolInstance;
import csnight.redis.monitor.redis.data.InfoCmdParser;
import csnight.redis.monitor.rest.rms.dto.RmsInsDto;
import csnight.redis.monitor.utils.BaseUtils;
import csnight.redis.monitor.utils.IdentifyUtils;
import csnight.redis.monitor.utils.RegexUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class RmsInsManageImpl {
    @Resource
    private RmsInsRepository rmsInsRepository;
    @Resource
    private SysUserRepository userRepository;

    /**
     * 功能描述: 查询全部实例
     *
     * @param put 是否更新缓存
     * @return java.util.List<com.alibaba.fastjson.JSONObject>
     * @author csnight
     * @since 2019-12-26 22:46
     */
    @Cacheable(value = "instances", condition = "#put.equals('false')")
    public List<JSONObject> GetInstances(String put) {
        List<JSONObject> res = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        List<RmsInstance> instances = rmsInsRepository.findAll(Sort.by(Sort.Direction.ASC, "ct"));
        //实例用户id-》用户名
        for (RmsInstance ins : instances) {
            ids.add(ins.getUser_id());
        }
        UserQueryExp exp = new UserQueryExp();
        exp.setIds(ids);
        List<SysUser> users = userRepository.findAll((root, criteriaQuery, criteriaBuilder) ->
                QueryAnnotationProcess.getPredicate(root, exp, criteriaBuilder));
        for (RmsInstance ins : instances) {
            JSONObject jo = JSONObject.parseObject(JSONObject.toJSONString(ins));
            String username = "";
            for (SysUser user : users) {
                if (ins.getUser_id().equals(user.getId())) {
                    username = user.getUsername();
                }
            }
            jo.put("user", username);
            res.add(jo);
        }
        return res;
    }

    /**
     * 功能描述: 根据用户查询实例
     *
     * @param user_id 用户给id
     * @return java.util.List<com.alibaba.fastjson.JSONObject>
     * @author csnight
     * @since 2019-12-26 22:46
     */
    @Cacheable(value = "instance", key = "#user_id")
    public List<JSONObject> GetInstanceByUser(String user_id) {
        List<JSONObject> res = new ArrayList<>();
        List<RmsInstance> instances = rmsInsRepository.findByUserId(user_id);
        for (RmsInstance ins : instances) {
            JSONObject jo = JSONObject.parseObject(JSONObject.toJSONString(ins));
            jo.put("user", BaseUtils.GetUserFromContext());
            res.add(jo);
        }
        return res;
    }

    /**
     * 功能描述: 实例搜索
     *
     * @param exp 实例查询条件
     * @return java.util.List<com.alibaba.fastjson.JSONObject>
     * @author csnight
     * @since 2019-12-26 22:46
     */
    public List<JSONObject> QueryBy(InsQueryExp exp) {
        List<JSONObject> res = new ArrayList<>();
        SysUser user = userRepository.findByUsername(BaseUtils.GetUserFromContext());
        exp.setUser_id(user.getId());
        //当用户具有开发者或超管角色 则为查询库中全部符合条件实例 否则为在当前用户实例中查询
        for (SysRole role : user.getRoles()) {
            if (role.getCode().equals("ROLE_DEV") || role.getCode().equals("ROLE_SUPER")) {
                exp.setUser_id("%");
                break;
            }
        }
        List<RmsInstance> instances = rmsInsRepository.findAll((root, criteriaQuery, criteriaBuilder) ->
                QueryAnnotationProcess.getPredicate(root, exp, criteriaBuilder), Sort.by(Sort.Direction.ASC, "ct"));
        //根据实例用户id 检索实例用户名
        if (exp.getUser_id().equals("%")) {
            Set<String> ids = new HashSet<>();
            for (RmsInstance ins : instances) {
                ids.add(ins.getUser_id());
            }
            UserQueryExp userQueryExp = new UserQueryExp();
            exp.setIds(ids);
            List<SysUser> users = userRepository.findAll((root, criteriaQuery, criteriaBuilder) ->
                    QueryAnnotationProcess.getPredicate(root, userQueryExp, criteriaBuilder));
            for (RmsInstance ins : instances) {
                JSONObject jo = JSONObject.parseObject(JSONObject.toJSONString(ins));
                String username = "";
                for (SysUser sysUser : users) {
                    if (ins.getUser_id().equals(sysUser.getId())) {
                        username = sysUser.getUsername();
                    }
                }
                jo.put("user", username);
                res.add(jo);
            }
        } else {
            for (RmsInstance ins : instances) {
                JSONObject jo = JSONObject.parseObject(JSONObject.toJSONString(ins));
                jo.put("user", user.getUsername());
                res.add(jo);
            }
        }
        return res;
    }

    /**
     * 功能描述: 新增实例
     *
     * @param dto 实例Dto
     * @return csnight.redis.monitor.db.jpa.RmsInstance
     * @author csnight
     * @since 2019-12-26 22:46
     */
    @Caching(evict = {@CacheEvict(value = "instances", beforeInvocation = true, allEntries = true),
            @CacheEvict(value = "instance", key = "#result.user_id", condition = "#result!=null")})
    public RmsInstance NewInstance(RmsInsDto dto) throws ConfigException, ConflictsException {
        RmsInstance ins = new RmsInstance();
        String user_id = userRepository.findIdByUsername(BaseUtils.GetUserFromContext());
        ins.setId(IdentifyUtils.getUUID());
        ins.setUser_id(user_id);
        ins.setIp(dto.getIp());
        ins.setPort(dto.getPort());
        ins.setInstance_name(dto.getName());
        if (!checkInstanceName(ins)) {
            throw new ConflictsException("Instance name conflict");
        }
        ins.setCt(new Date());
        PoolConfig config = BuildConfig(dto);
        config.setUser_id(user_id);
        config.setIns_id(ins.getId());
        config.checkMd5();
        //实例连接信息冲突检查
        RmsInstance insConflict = rmsInsRepository.findByUin(config.getUin());
        if (insConflict != null) {
            throw new ConfigException("Redis instance conflict");
        }
        ins.setUin(config.getUin());
        RedisPoolInstance pool = MultiRedisPool.getInstance().addNewPool(config);
        if (pool != null) {
            ins.setState(true);
            return parseInfo(ins, pool, config);
        } else {
            throw new ConfigException("Can not build a redis connection pool");
        }
    }

    /**
     * 功能描述: 删除实例
     *
     * @param ins_id 实例id
     * @return java.lang.String
     * @author csnight
     * @since 2019-12-26 22:46
     */
    @CacheEvict(value = {"instances", "instance"}, beforeInvocation = true, allEntries = true)
    public String DeleteInstance(String ins_id) {
        try {
            RmsInstance ins = rmsInsRepository.findOnly(ins_id);
            if (ins != null) {
                //关闭实例关联channel handler
                closeRelateChannelHandler(ins.getId());
                //TODO 停止关联定时任务
                //TODO 实例关联信息清除
                rmsInsRepository.delete(ins);
                boolean res = MultiRedisPool.getInstance().removePool(ins_id);
            }
            return "success";
        } catch (Exception ex) {
            return "failed";
        }
    }

    /**
     * 功能描述: 修改实例名称
     *
     * @param dto 实例Dto
     * @return csnight.redis.monitor.db.jpa.RmsInstance
     * @author csnight
     * @since 2019-12-26 22:46
     */
    @Caching(evict = {@CacheEvict(value = "instances", beforeInvocation = true, allEntries = true),
            @CacheEvict(value = "instance", key = "#result.user_id", condition = "#result!=null")})
    public RmsInstance ModifyInsName(RmsInsDto dto) throws ConflictsException {
        RmsInstance oldIns = rmsInsRepository.findOnly(dto.getId());
        if (oldIns != null) {
            //如果名称发生变化，则检查实例名称冲突
            if (!dto.getName().equals(oldIns.getInstance_name())) {
                oldIns.setInstance_name(dto.getName());
                if (!checkInstanceName(oldIns)) {
                    throw new ConflictsException("Instance name conflict");
                }
                return rmsInsRepository.save(oldIns);
            }
            return oldIns;
        }
        return null;
    }

    /**
     * 功能描述: 实例连接状态变更
     *
     * @param dto 实例Dto
     * @return csnight.redis.monitor.db.jpa.RmsInstance
     * @author csnight
     * @since 2019-12-26 22:47
     */
    @Caching(evict = {@CacheEvict(value = "instances", beforeInvocation = true, allEntries = true),
            @CacheEvict(value = "instance", key = "#result.user_id", condition = "#result!=null")})
    public RmsInstance ModifyInsState(RmsInsDto dto) throws ConfigException {
        RmsInstance oldIns = rmsInsRepository.findOnly(dto.getId());
        if (oldIns != null) {
            if (oldIns.isState() && !dto.isState()) {
                //连接池不存在，则直接设置实例为关闭状态并保存
                if (MultiRedisPool.getInstance().getPool(oldIns.getId()) == null) {
                    oldIns.setState(false);
                    return rmsInsRepository.save(oldIns);
                }
                //如果实例连接池存在，则关闭连接池并保存实例为关闭状态
                //TODO 停止关联定时任务
                closeRelateChannelHandler(oldIns.getId());
                boolean isShutdown = MultiRedisPool.getInstance().removePool(oldIns.getId());
                if (isShutdown) {
                    oldIns.setState(false);
                    return rmsInsRepository.save(oldIns);
                }
            } else {
                //新建连接池，连接成功则保存实例为已连接状态，更新实例元信息
                PoolConfig config = JSONObject.parseObject(oldIns.getConn(), PoolConfig.class);
                RedisPoolInstance pool = MultiRedisPool.getInstance().addNewPool(config);
                if (pool != null) {
                    oldIns.setState(true);
                    return parseInfo(oldIns, pool, config);
                }
            }
            return oldIns;
        }
        return null;
    }

    /**
     * 功能描述: 修改实例连接状态
     *
     * @param dto 实例DTO
     * @return csnight.redis.monitor.db.jpa.RmsInstance
     * @author csnight
     * @since 2019-12-26 22:47
     */
    @Caching(evict = {@CacheEvict(value = "instances", beforeInvocation = true, allEntries = true),
            @CacheEvict(value = "instance", key = "#result.user_id", condition = "#result!=null")})
    public RmsInstance ModifyInsConn(RmsInsDto dto) throws ConfigException {
        RmsInstance oldIns = rmsInsRepository.findOnly(dto.getId());
        if (oldIns != null) {
            //构建连接池连接配置
            PoolConfig config_old = JSONObject.parseObject(oldIns.getConn(), PoolConfig.class);
            PoolConfig config_new = BuildConfig(dto);
            // 设置用户id及实例id
            config_new.setIns_id(oldIns.getId());
            config_new.setUser_id(oldIns.getUser_id());
            //生成实例UIN
            config_old.checkMd5();
            config_new.checkMd5();
            //实例UIN冲突检查
            RmsInstance temExist = rmsInsRepository.findByUin(config_new.getUin());
            if (temExist != null) {
                //待变更实例uin与库中某实例相同（同一用户下，连接信息相同）且实例id不同，则待变更实例为用户已经存在的实例，实例变更冲突，抛出异常
                if (!temExist.getId().equals(oldIns.getId())) {
                    throw new ConfigException("Redis instance conflict");
                }
            }
            oldIns.setUin(config_new.getUin());
            //如实例存在连接池，则关闭连接并保存为关闭状态
            if (oldIns.isState() && MultiRedisPool.getInstance().getPool(oldIns.getId()) != null) {
                //TODO 停止关联定时任务
                closeRelateChannelHandler(oldIns.getId());
                boolean isShutdown = MultiRedisPool.getInstance().removePool(oldIns.getId());
                if (isShutdown) {
                    oldIns.setState(false);
                    oldIns = rmsInsRepository.save(oldIns);
                }
            }
            //根据新连接信息新建连接池，更新实例元信息，新建失败抛出异常
            RedisPoolInstance pool = MultiRedisPool.getInstance().addNewPool(config_new);
            if (pool != null) {
                oldIns.setState(true);
                return parseInfo(oldIns, pool, config_new);
            } else {
                throw new ConfigException("Can not build a redis connection pool");
            }

        }
        return null;
    }

    /**
     * 功能描述: 更新实例元信息
     *
     * @param ins_id 实例ID
     * @return csnight.redis.monitor.db.jpa.RmsInstance
     * @author csnight
     * @since 2019-12-26 22:47
     */
    @Caching(evict = {@CacheEvict(value = "instances", beforeInvocation = true, allEntries = true),
            @CacheEvict(value = "instance", key = "#result.user_id", condition = "#result!=null")})
    public RmsInstance UpdateInsMeta(String ins_id) throws ConfigException {
        RmsInstance oldIns = rmsInsRepository.findOnly(ins_id);
        if (oldIns != null) {
            PoolConfig config = JSONObject.parseObject(oldIns.getConn(), PoolConfig.class);
            RedisPoolInstance pool = MultiRedisPool.getInstance().getPool(oldIns.getId());
            //若连接池存在则直接查询元信息并更新
            if (pool != null) {
                return parseInfo(oldIns, pool, config);
            } else {
                //若不存在则新建连接池并更新状态
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

    /**
     * 功能描述: 程序退出时关闭连接池，并设置实例状态
     *
     * @param ids 具有连接池的实例id列表
     * @author csnight
     * @since 2019-12-26 22:47
     */
    public void ChangeAllState(Set<String> ids) {
        InsQueryExp exp = new InsQueryExp();
        exp.setIds(ids);
        List<RmsInstance> instances = rmsInsRepository.findAll((root, criteriaQuery, criteriaBuilder) ->
                QueryAnnotationProcess.getPredicate(root, exp, criteriaBuilder));
        for (RmsInstance instance : instances) {
            instance.setState(false);
        }
        rmsInsRepository.saveAll(instances);
    }

    /**
     * 功能描述: redis实例元信息查询
     *
     * @param ins    redis 实体
     * @param pool   redis 连接池
     * @param config 实例连接信息
     * @return csnight.redis.monitor.db.jpa.RmsInstance
     * @author csnight
     * @since 2019-12-26 22:47
     */
    private RmsInstance parseInfo(RmsInstance ins, RedisPoolInstance pool, PoolConfig config) throws ConfigException {
        try {
            Map<String, String> info = InfoCmdParser.GetInstanceInfo(pool, "Server");
            //根据连接池类型设置实例连接类型字段
            ins.setType("Standalone");
            if (config.getSentinels().size() > 0) {
                ins.setIp(pool.getHP().getHost());
                ins.setPort(pool.getHP().getPort());
                ins.setType("SentinelsCluster");
            }
            ins.setArch_bits(Integer.parseInt(info.get("arch_bits")));
            ins.setOs(info.get("os"));
            ins.setMode(info.get("redis_mode"));
            ins.setProc_id(Integer.parseInt(info.get("process_id")));
            ins.setUptime_in_seconds(Integer.parseInt(info.get("uptime_in_seconds")));
            ins.setHz(Integer.parseInt(info.get("hz")));
            ins.setVersion(info.get("redis_version"));
            ins.setConfig(info.get("executable"));
            ins.setExec(info.get("config_file"));
            //设置实例角色
            if (ins.getMode().equals("sentinel")) {
                ins.setRole("sentinel");
            } else {
                ins.setRole(InfoCmdParser.GetInfoBySectionKey(pool, "Replication", "role"));
            }
            ins.setConn(JSONObject.toJSONString(config));
            return rmsInsRepository.save(ins);
        } catch (Exception ex) {
            //实例信息检查失败则关闭连接池，并设置实例状态为未连接，存储实例连接信息
            MultiRedisPool.getInstance().removePool(ins.getId());
            ins.setState(false);
            ins.setConn(JSONObject.toJSONString(config));
            rmsInsRepository.save(ins);
            throw new ConfigException("Redis connection failed", ex.getCause());
        }
    }

    /**
     * 功能描述: 根据redis连接信息构建连接池配置实例
     *
     * @param dto redis 连接信息
     * @return csnight.redis.monitor.redis.pool.PoolConfig
     * @author csnight
     * @since 2019-12-26 22:47
     */
    private PoolConfig BuildConfig(RmsInsDto dto) throws ConfigException {
        try {
            if (dto.getIp() != null && RegexUtils.checkIp(dto.getIp())) {
                return JSONObject.parseObject(JSONObject.toJSONString(dto), PoolConfig.class);
            } else if (dto.getMaster() != null && dto.getSentinels().size() > 0) {
                return JSONObject.parseObject(JSONObject.toJSONString(dto), PoolConfig.class);
            }
        } catch (Exception ex) {
            throw new ConfigException("Redis Config Read Error" + ex.getMessage());
        }
        throw new ConfigException("Can not parse redis connection config");
    }

    /**
     * 功能描述: 检查同一用户下实例名称冲突
     *
     * @param ins Redis实例
     * @return boolean
     * @author csnight
     * @since 2019-12-26 22:47
     */
    private boolean checkInstanceName(RmsInstance ins) {
        boolean valid = true;
        RmsInstance insExist = rmsInsRepository.findByInstanceName(ins.getInstance_name());
        if (insExist != null && insExist.getUser_id().equals(ins.getUser_id())) {
            valid = false;
        }
        return valid;
    }

    private void closeRelateChannelHandler(String ins) {
        MsgBus.getIns().getChannels().forEach((cid, chEnt) -> {
            chEnt.getHandlers().forEach((hid, handler) -> {
                if (handler.getIns().equals(ins)) {
                    handler.destroy();
                }
            });
        });
    }
}
