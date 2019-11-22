package com.csnight.redis.monitor.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Component
@Aspect
@Order(1)
public class LogAspectAnnotation {
    private Logger logger = LoggerFactory.getLogger(LogAspectAnnotation.class);

    @Pointcut("@annotation(com.csnight.redis.monitor.aop.LogAsync)")
    public void aop_cut() {
    }

    @Before("aop_cut()")
    private void doBefore(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert attributes != null;
        HttpServletRequest req = attributes.getRequest();
        String s = req.getRemoteAddr() + " " + req.getMethod() + " " + req.getRequestURI() + " Parameters => {}";
        LogAsyncPool.getIns().offer(s);
    }
}
