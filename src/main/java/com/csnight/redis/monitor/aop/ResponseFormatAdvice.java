package com.csnight.redis.monitor.aop;

import com.csnight.redis.monitor.utils.RespTemplate;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Objects;

@ControllerAdvice
public class ResponseFormatAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter methodParameter, Class aClass) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object o, MethodParameter methodParameter, MediaType mediaType, Class aClass, ServerHttpRequest req, ServerHttpResponse resp) {
        if (!req.getURI().getPath().contains("swagger") && !req.getURI().toString().contains("v2/api-doc")) {
            ServletServerHttpResponse ssr = (ServletServerHttpResponse) resp;
            int s = ssr.getServletResponse().getStatus();
            String method = Objects.requireNonNull(methodParameter.getMethod()).getName();
            if (!(o instanceof RespTemplate)) {
                return o;
            }
            RespTemplate respTemplate = (RespTemplate) o;
            if (respTemplate.getMessage().equals("failed")) {
                return new RespTemplate(s, HttpStatus.BAD_REQUEST, respTemplate.getMessage(), req.getURI().getPath(), method);
            } else
                return new RespTemplate(s, respTemplate.getCode(), respTemplate.getMessage(), req.getURI().getPath(), method);
        }
        return o;
    }
}
