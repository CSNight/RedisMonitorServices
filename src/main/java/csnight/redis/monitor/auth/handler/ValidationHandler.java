package csnight.redis.monitor.auth.handler;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.exception.ValidateException;
import csnight.redis.monitor.utils.RespTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.rememberme.CookieTheftException;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class ValidationHandler extends OncePerRequestFilter implements Filter {

    @Resource
    private LoginFailureHandler failureHandler;
    @Resource
    private LoginSuccessHandler successHandler;
    private AntPathRequestMatcher requestMatcher = new AntPathRequestMatcher("/auth/sign", "POST");


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String origin = request.getHeader("Origin");
        response.setHeader("Access-Control-Allow-Origin", origin);
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS");
        response.setHeader("Access-Control-Max-Age", "86400");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
        if (requestMatcher.matches(request)) {
            //禁止重复登陆
            if (successHandler.getLoginUserList().containsKey(request.getParameter("username"))) {
                String session_id = successHandler.getLoginUserList().get(request.getParameter("username"));
                String request_session_id = request.getSession().getId();
                if (session_id.equals(request_session_id)) {
                    response.setContentType("application/json;charset=UTF-8");
                    JSONObject jo_res = new JSONObject();
                    jo_res.put("msg", "Already Login");
                    jo_res.put("username", request.getParameter("username"));
                    response.getWriter().write(JSONObject.toJSONString(
                            new RespTemplate(200, HttpStatus.OK, jo_res, "/auth/sign", "Login")));
                    return;
                }
            }
            //账户锁定则跳转登录失败处理器处理
            if (failureHandler.getLock_list().containsKey(request.getParameter("username"))) {
                this.failureHandler.onAuthenticationFailure(request, response, new ValidateException(""));
                return;
            }
            try {
                validate(request);
            } catch (ValidateException e) {
                unsuccessfulAuthentication(request, response, e);
                return;
            }
        }
        try {
            filterChain.doFilter(request, response);
        } catch (CookieTheftException e) {
            unsuccessfulAuthentication(request, response, e);
        }
    }

    private void validate(HttpServletRequest request) {
        Object codeInSession = request.getSession().getAttribute("ValCode");
        String codeInRequest = request.getParameter("ValCode");
        if (codeInRequest == null || codeInSession == null) {
            throw new ValidateException("会话过期，刷新后重试");
        }
        if (codeInRequest.equals("")) {
            throw new ValidateException("验证码的值不能为空");
        }
        if (!codeInRequest.toLowerCase().equals(codeInSession.toString().toLowerCase())) {
            throw new ValidateException("验证码不匹配");
        }
    }

    private void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        this.failureHandler.onAuthenticationFailure(request, response, failed);
    }
}