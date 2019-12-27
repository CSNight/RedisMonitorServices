package csnight.redis.monitor.busi.rms;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.db.jpa.RmsCmdPermits;
import csnight.redis.monitor.db.jpa.RmsInstance;
import csnight.redis.monitor.db.repos.RmsCmdRepository;
import csnight.redis.monitor.db.repos.RmsInsRepository;
import csnight.redis.monitor.db.repos.SysUserRepository;
import csnight.redis.monitor.exception.ConflictsException;
import csnight.redis.monitor.exception.ValidateException;
import csnight.redis.monitor.redis.pool.MultiRedisPool;
import csnight.redis.monitor.redis.pool.PoolConfig;
import csnight.redis.monitor.rest.rms.dto.InsRightsDto;
import csnight.redis.monitor.utils.BaseUtils;
import csnight.redis.monitor.utils.IdentifyUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class RmsInsRightsImpl {
    @Resource
    private RmsInsRepository rmsInsRepository;
    @Resource
    private SysUserRepository userRepository;
    @Resource
    private RmsCmdRepository rmsCmdRepository;

    /**
     * 功能描述:获取所有实例授权（管理员）
     *
     * @author csnight
     * @since 2019/12/27 9:27
     */
    @Cacheable(value = "ins_permits")
    public List<JSONObject> GetPermitsList() {
        List<RmsCmdPermits> cmdPermits = rmsCmdRepository.findAll();
        List<JSONObject> res = new ArrayList<>();
        for (RmsCmdPermits permits : cmdPermits) {
            String username = userRepository.findUsernameById(permits.getUser_id());
            RmsInstance instance = rmsInsRepository.findOnly(permits.getIns_id());
            if (instance != null) {
                JSONObject ins = JSONObject.parseObject(JSONObject.toJSONString(instance));
                ins.put("authorize", username);
                ins.put("cmd_right", permits);
                res.add(ins);
            }
        }
        return res;
    }

    /**
     * 功能描述: 获取用户所有授权实例
     *
     * @param user_id 用户id
     * @return : java.util.List<com.alibaba.fastjson.JSONObject>
     * @author csnight
     * @since 2019/12/27 8:56
     */
    @Cacheable(value = "ins_belongs")
    public List<JSONObject> GetBelongs(String user_id) {
        List<RmsInstance> instances = rmsInsRepository.findByBelong(user_id);
        List<JSONObject> res = new ArrayList<>();
        for (RmsInstance instance : instances) {
            //去除用户所有权实例，仅返回授权用户实例
            if (instance.getUser_id().equals(user_id)) {
                continue;
            }
            String username = userRepository.findUsernameById(instance.getUser_id());
            JSONObject ins = JSONObject.parseObject(JSONObject.toJSONString(instance));
            RmsCmdPermits cmdRights = rmsCmdRepository.findByUserIdAndInsId(instance.getUser_id(), instance.getId());
            if (cmdRights != null) {
                ins.put("cmd_right", cmdRights);
            }
            ins.put("authorize", username);
            res.add(ins);
        }
        return res;
    }

    /**
     * 功能描述: 增加实例授权及关联命令授权 一次一个
     *
     * @param dto 授权信息Dto
     * @return : csnight.redis.monitor.db.jpa.RmsCmdPermits
     * @author csnight
     * @since 2019/12/27 8:57
     */
    @CacheEvict(value = {"ins_permits", "ins_belongs"}, beforeInvocation = true, allEntries = true)
    public RmsCmdPermits AddInsPermits(InsRightsDto dto) throws ConflictsException {
        String user_id = userRepository.findIdByUsername(dto.getUsername());
        if (user_id == null) {
            throw new ValidateException("User not found");
        }
        RmsInstance instance = getIns(dto, user_id);
        String ins_id = instance.getId();
        //实例授权
        if (!instance.getUser_id().equals(user_id)) {
            ins_id = IdentifyUtils.getUUID();
            instance.setId(ins_id);
            instance.setUser_id(user_id);
            PoolConfig joConfig = JSONObject.parseObject(JSONObject.toJSONString(instance.getConfig()), PoolConfig.class);
            joConfig.setUser_id(user_id);
            joConfig.setIns_id(instance.getId());
            joConfig.checkMd5();
            instance.setUin(joConfig.getUin());
            instance.setConfig(JSONObject.toJSONString(joConfig));
            rmsInsRepository.save(instance);
        }
        if (!checkCmdConflict(dto, user_id)) {
            throw new ConflictsException("Command permissions to this user and instance already exists");
        }
        RmsCmdPermits permits = new RmsCmdPermits();
        permits.setIns_id(ins_id);
        permits.setUser_id(user_id);
        permits.setCmd(dto.getCommands());
        permits.setCreate_time(new Date());
        permits.setCreate_user(BaseUtils.GetUserFromContext());
        return rmsCmdRepository.save(permits);
    }

    /**
     * 功能描述: 修改实例命令授权
     *
     * @param dto 实例授权Dto
     * @return : csnight.redis.monitor.db.jpa.RmsCmdPermits
     * @author csnight
     * @since 2019/12/27 9:07
     */
    @CacheEvict(value = {"ins_permits", "ins_belongs"}, beforeInvocation = true, allEntries = true)
    public RmsCmdPermits ModifyInsRight(InsRightsDto dto) {
        String user_id = userRepository.findIdByUsername(dto.getUsername());
        if (user_id == null) {
            throw new ValidateException("User not found");
        }
        RmsCmdPermits cmdPermits = rmsCmdRepository.findByUserIdAndInsId(user_id, dto.getIns_id());
        if (cmdPermits == null) {
            throw new ValidateException("Instance permission not found");
        }
        cmdPermits.setCmd(dto.getCommands());
        return rmsCmdRepository.save(cmdPermits);
    }

    /**
     * 功能描述: 删除实例授权
     *
     * @param id 授权id
     * @return : java.lang.String
     * @author csnight
     * @since 2019/12/27 9:24
     */
    @CacheEvict(value = {"ins_permits", "ins_belongs"}, beforeInvocation = true, allEntries = true)
    public String DeleteInsRight(String id) {
        Optional<RmsCmdPermits> optPermit = rmsCmdRepository.findById(id);
        if (optPermit.isPresent()) {
            RmsCmdPermits permits = optPermit.get();
            MultiRedisPool.getInstance().removePool(permits.getIns_id());
            RmsInstance optIns = rmsInsRepository.findOnly(permits.getIns_id());
            if (optIns != null) {
                rmsInsRepository.delete(optIns);
            }
            rmsCmdRepository.delete(permits);
            return "success";
        }
        return "failed";
    }

    /**
     * 功能描述: 获取授权实例，如已授权则直接返回，未授权则查询该用户实例返回作为待授权实例
     *
     * @param dto     实例授权dto
     * @param user_id 实例所有者id
     * @return : csnight.redis.monitor.db.jpa.RmsInstance
     * @author csnight
     * @since 2019/12/27 8:58
     */
    private RmsInstance getIns(InsRightsDto dto, String user_id) {
        RmsInstance insExist = rmsInsRepository.findOnly(dto.getIns_id());
        if (insExist == null) {
            throw new ValidateException("Instance dose not exist");
        }
        RmsInstance instance = rmsInsRepository.findByUserIdAndBelong(user_id, insExist.getUser_id());
        if (instance == null) {
            return insExist;
        } else {
            return instance;
        }
    }

    private boolean checkCmdConflict(InsRightsDto dto, String user_id) {
        RmsCmdPermits exist = rmsCmdRepository.findByUserIdAndInsId(user_id, dto.getIns_id());
        return exist == null;
    }
}
