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

@Component
@Aspect
@Order(1)
public class LogAspectCommon {
    private Logger logger = LoggerFactory.getLogger(LogAspectCommon.class);

    @Pointcut("execution(public * com.csnight.redis.monitor.rest..*(..))")
    public void aop_cut() {
    }

    @Before("aop_cut()")
    private void doBefore(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    }
}
