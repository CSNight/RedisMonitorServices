package com.redis.monitor.auth.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;

@Component
public class SignOutHandler implements LogoutHandler {
    private static Logger _log = LoggerFactory.getLogger(SignOutHandler.class);
    @Resource
    private LoginSuccessHandler successHandler;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String session_id = request.getSession().getId();
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
        _log.info(remove_key + ":账户登出成功" + new Date());
    }

    private void InitializeRedisDbAndJobs() {
        //TODO 增加用户数据库连接逻辑及定时任务终止逻辑
    }
}
