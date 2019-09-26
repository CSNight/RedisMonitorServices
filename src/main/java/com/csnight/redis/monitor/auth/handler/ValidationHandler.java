package com.csnight.redis.monitor.auth.handler;

import com.csnight.redis.monitor.auth.config.ValidateCodeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class ValidationHandler extends OncePerRequestFilter implements Filter {
    private Map<String, Integer> LoginUserList = new HashMap<>();
    private Map<String, Integer> LoginFailList = new HashMap<>();
    @Autowired
    private AuthenticationFailureHandler failureHandler;
    @Autowired
    private LoginSuccessHandler successHandler;

    private AntPathRequestMatcher requestMatcher = new AntPathRequestMatcher("/auth/sign", "POST");
    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    public Map<String, Integer> getLoginUserList() {
        return LoginUserList;
    }

    public void setLoginUserList(Map<String, Integer> loginUserList) {
        LoginUserList = loginUserList;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (successHandler.getLoginUserList().containsKey(request.getParameter("username"))) {
            String session_id = successHandler.getLoginUserList().get(request.getParameter("username"));
            String request_session_id = request.getSession().getId();
            if (session_id.equals(request_session_id)) {
                redirectStrategy.sendRedirect(request, response, "/auth/user_info");
                return;
            }
        }
        if (requestMatcher.matches(request)) {
            try {
                validate(request);
            } catch (ValidateCodeException e) {
                unsuccessfulAuthentication(request, response, e);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private void validate(HttpServletRequest request) {

        Object codeInSession = request.getSession().getAttribute("ValCode");
        String codeInRequest = request.getParameter("ValCode");
        if (codeInRequest == null || codeInSession == null) {
            throw new ValidateCodeException("会话过期，刷新后重试");
        }
        if (codeInRequest.equals("")) {
            throw new ValidateCodeException("验证码的值不能为空");
        }
        if (!codeInRequest.toLowerCase().equals(codeInSession.toString().toLowerCase())) {
            throw new ValidateCodeException("验证码不匹配");
        }
    }

    private void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        this.failureHandler.onAuthenticationFailure(request, response, failed);
    }
}