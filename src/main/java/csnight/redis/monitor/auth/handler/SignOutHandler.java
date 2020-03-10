package csnight.redis.monitor.auth.handler;

import csnight.redis.monitor.db.jpa.RmsInstance;
import csnight.redis.monitor.db.jpa.SysUser;
import csnight.redis.monitor.db.repos.RmsInsRepository;
import csnight.redis.monitor.db.repos.SysUserRepository;
import csnight.redis.monitor.msg.MsgBus;
import csnight.redis.monitor.redis.pool.MultiRedisPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class SignOutHandler implements LogoutHandler {
    private static Logger _log = LoggerFactory.getLogger(SignOutHandler.class);
    @Resource
    private LoginSuccessHandler successHandler;
    @Resource
    private SysUserRepository userRepository;
    @Resource
    private RmsInsRepository insRepository;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String session_id = request.getSession().getId();
        String remove_key = "";
        for (Map.Entry<String, String> ent : successHandler.getLoginUserList().entrySet()) {
            if (ent.getValue().equals(session_id)) {
                remove_key = ent.getKey();
            }
        }
        if (!remove_key.equals("")) {
            successHandler.getLoginUserList().remove(remove_key);
            ShutdownRedisDbAndJobs(remove_key);
            ShutdownChannels(remove_key);
        }
        _log.info(remove_key + ":账户登出成功" + new Date());
    }

    private void ShutdownRedisDbAndJobs(String username) {
        try {
            SysUser user = userRepository.findByUsername(username);
            if (user != null) {
                boolean st = MultiRedisPool.getInstance().shutdownUserPools(user.getId());
                if (st) {
                    List<RmsInstance> instances = insRepository.findByUserId(user.getId());
                    for (RmsInstance ins : instances) {
                        ins.setState(false);
                    }
                    insRepository.saveAll(instances);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            _log.error(ex.getMessage());
        }
    }

    //关闭用户websocket管道
    private void ShutdownChannels(String username) {
        try {
            SysUser user = userRepository.findByUsername(username);
            if (user != null) {
                MsgBus.getIns().ClearUserChannel(user.getId());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            _log.error(ex.getMessage());
        }
    }
}
