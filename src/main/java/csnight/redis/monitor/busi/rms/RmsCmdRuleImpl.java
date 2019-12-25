package csnight.redis.monitor.busi.rms;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.db.jpa.RmsCmdPermits;
import csnight.redis.monitor.db.jpa.RmsInstance;
import csnight.redis.monitor.db.repos.RmsCmdRepository;
import csnight.redis.monitor.db.repos.RmsInsRepository;
import csnight.redis.monitor.db.repos.SysUserRepository;
import csnight.redis.monitor.exception.ConflictsException;
import csnight.redis.monitor.exception.ValidateException;
import csnight.redis.monitor.redis.pool.PoolConfig;
import csnight.redis.monitor.rest.rms.dto.InsRightsDto;
import csnight.redis.monitor.utils.BaseUtils;
import csnight.redis.monitor.utils.IdentifyUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class RmsCmdRuleImpl {
    @Resource
    private RmsInsRepository rmsInsRepository;
    @Resource
    private SysUserRepository userRepository;
    @Resource
    private RmsCmdRepository rmsCmdRepository;

    public List<RmsCmdPermits> GetPermitsList() {
        return rmsCmdRepository.findAll();
    }

    public RmsCmdPermits AddCmdPermits(InsRightsDto dto) throws ConflictsException {
        String user_id = userRepository.findIdByUsername(dto.getUsername());
        if (user_id == null) {
            throw new ValidateException("User not found");
        }
        RmsInstance instance = getIns(dto, user_id);
        String ins_id = instance.getId();
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

    private RmsInstance getIns(InsRightsDto dto, String user_id) {
        Optional<RmsInstance> insExist = rmsInsRepository.findById(dto.getIns_id());
        if (!insExist.isPresent()) {
            throw new ValidateException("Instance dose not exist");
        }
        RmsInstance instance = rmsInsRepository.findByUserIdAndBelong(user_id, insExist.get().getUser_id());
        if (instance == null) {
            return insExist.get();
        } else {
            return instance;
        }
    }

    private boolean checkCmdConflict(InsRightsDto dto, String user_id) {
        RmsCmdPermits exist = rmsCmdRepository.findByUserIdAndInsId(user_id, dto.getIns_id());
        return exist == null;
    }
}
