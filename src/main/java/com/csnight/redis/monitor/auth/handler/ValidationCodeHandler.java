package com.csnight.redis.monitor.auth.handler;

import com.csnight.redis.monitor.auth.config.ValidateCodeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class ValidationCodeHandler extends OncePerRequestFilter implements Filter {
    @Autowired
    private AuthenticationFailureHandler failureHandler;

    private AntPathRequestMatcher requestMatcher = new AntPathRequestMatcher("/auth/sign", "POST");


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
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
        String codeInSession = request.getSession().getAttribute("ValCode").toString();
        String codeInRequest = request.getParameter("ValCode");
        if (codeInRequest == null) {
            throw new ValidateCodeException("验证码不存在");
        }
        if (codeInRequest.equals("")) {
            throw new ValidateCodeException("验证码的值不能为空");
        }
        if (!codeInRequest.toLowerCase().equals(codeInSession.toLowerCase())) {
            throw new ValidateCodeException("验证码不匹配");
        }
    }

    private void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        this.failureHandler.onAuthenticationFailure(request, response, failed);
    }
}