package csnight.redis.monitor.busi.rms;

import csnight.redis.monitor.db.repos.RmsInsRepository;
import csnight.redis.monitor.db.repos.SysUserRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class RmsCmdRuleImpl {
    @Resource
    private RmsInsRepository rmsInsRepository;
    @Resource
    private SysUserRepository userRepository;
}
