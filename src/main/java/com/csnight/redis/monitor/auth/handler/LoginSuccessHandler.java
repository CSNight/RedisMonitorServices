package com.csnight.redis.monitor.auth.handler;

import com.csnight.redis.monitor.auth.jpa.SysUser;
import com.csnight.redis.monitor.auth.repos.SysUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Component
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private SysUserRepository sysUserRepository;
    private Map<String, String> LoginUserList = new HashMap<>();

    public Map<String, String> getLoginUserList() {
        return LoginUserList;
    }

    public LoginSuccessHandler(SysUserRepository sysUserRepository) {
        this.sysUserRepository = sysUserRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        SysUser sysUser = sysUserRepository.findByUsername(userDetails.getUsername());
        sysUser.setLogin_times(sysUser.getLogin_times() + 1);
        sysUser.setLast_login(new Date());
        sysUserRepository.save(sysUser);
        LoginUserList.put(sysUser.getUsername(), request.getSession().getId());
        super.setDefaultTargetUrl("/auth/user_info");
        super.onAuthenticationSuccess(request, response, authentication);
    }

    private void InitializeRedisDbAndJobs() {
        //TODO 增加用户数据库连接逻辑及定时任务初始化逻辑
    }
}
