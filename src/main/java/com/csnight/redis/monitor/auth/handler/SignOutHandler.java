package com.csnight.redis.monitor.auth.handler;

import com.csnight.redis.monitor.auth.config.JdbcTokenRepositoryExt;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class SignOutHandler implements LogoutHandler {

    private LoginSuccessHandler successHandler;



    public SignOutHandler(LoginSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }



    private void InitializeRedisDbAndJobs() {
        //TODO 增加用户数据库连接逻辑及定时任务终止逻辑

    }

    @Override
    public void logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) {
        String session_id = httpServletRequest.getSession().getId();
        String remove_key = "";
        for (Map.Entry<String, String> ent : successHandler.getLoginUserList().entrySet()) {
            if (ent.getValue().equals(session_id)) {
                remove_key = ent.getKey();
            }
        }
        //TODO 查询persisit 表 获取remember-me 记录 综合判断
        if (!remove_key.equals("")) {
            successHandler.getLoginUserList().remove(remove_key);
        }
    }
}
