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
public class LogAspectCommon {
    private Logger logger = LoggerFactory.getLogger(LogAspectCommon.class);

    private ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Pointcut("execution(public * com.csnight.redis.monitor.controller..*(..))")
    public void aop_cut() {
    }

    @Before("aop_cut()")
    private void doBefore(JoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert attributes != null;
        HttpServletRequest req = attributes.getRequest();
        Object[] args = joinPoint.getArgs();
        StringBuilder params = new StringBuilder();
        for (Object arg : args) {
            params.append(arg.toString());
            params.append(";");
        }
        startTime.set(System.currentTimeMillis());
        logger.info(req.getRemoteAddr() + " " + req.getMethod() + " " + req.getRequestURI() + " Parameters => {}", params.toString());
    }
}
