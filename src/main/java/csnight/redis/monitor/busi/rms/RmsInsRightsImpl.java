package csnight.redis.monitor.busi.rms;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.db.jpa.RmsCmdPermits;
import csnight.redis.monitor.db.repos.RmsCmdRepository;
import csnight.redis.monitor.db.repos.SysUserRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class RmsInsRightsImpl {
    @Resource
    private SysUserRepository userRepository;
    @Resource
    private RmsCmdRepository rmsCmdRepository;

    public List<JSONObject> GetAllCmdAuthorities() {
        List<RmsCmdPermits> permits = rmsCmdRepository.findAll();
        List<JSONObject> authorities = new ArrayList<>();
        for (RmsCmdPermits cmdPermit : permits) {
            String username = userRepository.findUsernameById(cmdPermit.getUser_id());
            JSONObject cmdJo = JSONObject.parseObject(JSONObject.toJSONString(cmdPermit));
            cmdJo.put("username", username);
            authorities.add(cmdJo);
        }
        return authorities;
    }
}