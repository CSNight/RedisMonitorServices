package com.csnight.redis.monitor.auth.handler;

import com.alibaba.fastjson.JSONObject;
import com.csnight.redis.monitor.utils.RespTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class CusLogoutSuccessHandler extends HttpStatusReturningLogoutSuccessHandler {
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        if (authentication == null) {
            return;
        }
        String username = authentication.getName();
        response.setContentType("application/json;charset=UTF-8");
        JSONObject jo_res = new JSONObject();
        jo_res.put("msg", username + " logout Success");
        jo_res.put("username", username);
        String origin = request.getHeader("Origin");
        response.setHeader("Access-Control-Allow-Origin", origin);
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS");
        response.setHeader("Access-Control-Max-Age", "86400");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
        response.getWriter().write(JSONObject.toJSONString(new RespTemplate(200, HttpStatus.OK, jo_res, "/auth/logout", "Logout")));
        super.onLogoutSuccess(request, response, authentication);
        PrintWriter out = response.getWriter();
        out.close();
    }
}
