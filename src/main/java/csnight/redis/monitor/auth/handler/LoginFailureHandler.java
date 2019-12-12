package csnight.redis.monitor.auth.handler;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.db.jpa.SysUser;
import csnight.redis.monitor.db.repos.SysUserRepository;
import csnight.redis.monitor.quartz.JobFactory;
import csnight.redis.monitor.quartz.config.JobConfig;
import csnight.redis.monitor.quartz.jobs.Job_UnlockAccount;
import csnight.redis.monitor.utils.IdentifyUtils;
import csnight.redis.monitor.utils.RespTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    private static Logger _log = LoggerFactory.getLogger(SimpleUrlAuthenticationFailureHandler.class);
    @Resource
    private SysUserRepository sysUserRepository;
    @Resource
    private JobFactory jobFactory;
    private Map<String, Map<String, Integer>> LoginFailList = new ConcurrentHashMap<>();
    private Map<String, Long> lock_list = new ConcurrentHashMap<>();

    public Map<String, Map<String, Integer>> getLoginFailList() {
        return LoginFailList;
    }

    public LoginFailureHandler() {
        this.setDefaultFailureUrl("/auth/failed");
    }

    public Map<String, Long> getLock_list() {
        return lock_list;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
        String username = request.getParameter("username");
        String exception_msg = e.getMessage();
        if (checkAttemptLoginUser(username)) {
            if (LoginFailList.containsKey(username)) {
                Map<String, Integer> session_fails = LoginFailList.get(username);
                String session_id = request.getSession().getId();
                if (session_fails.containsKey(session_id)) {
                    session_fails.put(session_id, session_fails.get(session_id) + 1);
                } else {
                    session_fails.put(session_id, 1);
                }
                int total_fails = 0;
                for (String key : session_fails.keySet()) {
                    total_fails += session_fails.get(key);
                }
                _log.warn(username + ":账户尝试登陆错误" + total_fails + "次");
                if (total_fails >= 5) {
                    SysUser sysUser = sysUserRepository.findByUsername(username);
                    if (sysUser != null) {
                        sysUser.setEnabled(false);
                        sysUser.setLock_by("lockByFails");
                        sysUserRepository.save(sysUser);
                        unLockJob(username);
                        LoginFailList.remove(username);
                        exception_msg = "错误次数过多，账户已锁定，解锁时间60秒后";
                    }
                }
            } else {
                Map<String, Integer> session_fails = new HashMap<>();
                session_fails.put(request.getSession().getId(), 1);
                LoginFailList.put(username, session_fails);
                _log.warn(username + ":账户尝试登陆错误" + 1 + "次");
            }
        }
        if (username != null && lock_list.containsKey(username)) {
            long expire = (lock_list.get(username) - new Date().getTime()) / 1000;
            exception_msg = "错误次数过多，账户已锁定，解锁时间" + expire + "秒后";
            if (expire == 0) {
                exception_msg = "账户锁定已解除";
            }
        }
        _log.error(exception_msg);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSONObject.toJSONString(
                new RespTemplate(400, HttpStatus.BAD_REQUEST, exception_msg, "/auth/sign", "Login")));
        //super.onAuthenticationFailure(request, response, exception);
    }

    private void unLockJob(String username) {
        JobConfig jobConfigBase = new JobConfig();
        jobConfigBase.setJobName(IdentifyUtils.getUUID());
        jobConfigBase.setJobGroup("unlock");
        jobConfigBase.setInvokeParam(username);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("identity", jobConfigBase.getJobName());
        jsonObject.put("description", "");
        jsonObject.put("triggerGroup", "unlock");
        jsonObject.put("interval", 10);
        jsonObject.put("repeatCount", 1);
        jsonObject.put("startAt", new Date(new Date().getTime() + 60000));
        jobConfigBase.setTriggerConfig(jsonObject.toJSONString());
        jobConfigBase.setTriggerType(0);
        jobFactory.AddJob(jobConfigBase, Job_UnlockAccount.class);
        lock_list.put(username, new Date().getTime() + 60000);
    }

    private boolean checkAttemptLoginUser(String username) {
        boolean isEffect = true;
        if (username == null) {
            isEffect = false;
        } else if (username.equals("")) {
            isEffect = false;
        } else if (sysUserRepository.findByUsername(username) == null) {
            isEffect = false;
        } else if (lock_list.containsKey(username)) {
            isEffect = false;
        }
        return isEffect;
    }
}
