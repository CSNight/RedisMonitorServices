package com.csnight.redis.monitor.aop;

import com.csnight.redis.monitor.utils.JSONUtil;
import com.csnight.redis.monitor.utils.RespTemplate;
import com.csnight.redis.monitor.utils.ThrowableUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.nio.file.AccessDeniedException;

@Component
@Aspect
@Order(2)
public class LogAspectHttp {
    private Logger logger = LoggerFactory.getLogger(LogAspectHttp.class);

    private ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Pointcut("execution(public * com.csnight.redis.monitor.rest..*(..))")
    public void webLog() {
    }

    @Around("webLog()")
    public Object logHandler(ProceedingJoinPoint process) {
        MethodSignature methodSignature = (MethodSignature) process.getSignature();
        Method method = methodSignature.getMethod();
        String methodName = method.getName();
        String className = method.getDeclaringClass().getName();
        Object[] args = process.getArgs();
        StringBuilder params = new StringBuilder();
        for (Object arg : args) {
            if (arg != null) {
                params.append(arg.toString());
                params.append(";");
            }
        }
        startTime.set(System.currentTimeMillis());
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert attributes != null;
        HttpServletRequest req = attributes.getRequest();
        logger.info(req.getRemoteAddr() + " " + req.getMethod() + " " + req.getRequestURI() + " Parameters => {}", params.toString());
        Object result = null;
        try {
            result = process.proceed();
        } catch (Throwable throwable) {
            HttpServletResponse rep = attributes.getResponse();
            long costTime = System.currentTimeMillis() - startTime.get();
            assert rep != null;
            logger.error("\r\nCost:{}ms Status:" + rep.getStatus() +
                    "\r\nClass:{} => {}\r\n" +
                    "Error => {}", costTime, className, methodName, throwable.getMessage());
            if (throwable instanceof AccessDeniedException) {
                result = new RespTemplate(403, HttpStatus.FORBIDDEN, throwable.getMessage(), req.getRequestURI(), req.getMethod());
            } else {
                result = new RespTemplate(500, HttpStatus.INTERNAL_SERVER_ERROR, throwable.getMessage(), req.getRequestURI(), req.getMethod());
            }
        }
        HttpServletResponse rep = attributes.getResponse();
        long costTime = System.currentTimeMillis() - startTime.get();
        assert rep != null;
        Object wrapRes = "";
        if (result instanceof RespTemplate) {
            wrapRes = JSONUtil.object2json(result);
        } else {
            wrapRes = result;
        }
        if (methodName.equals("GetIcons")) {
            logger.info("\r\nCost:{}ms Status:" + rep.getStatus() +
                    "\r\nClass:{}=>{}\r\n" +
                    "Response => {}", costTime, className, methodName, "icons");
        } else {
            logger.info("\r\nCost:{}ms Status:" + rep.getStatus() +
                    "\r\nClass:{}=>{}\r\n" +
                    "Response => {}", costTime, className, methodName, wrapRes);
        }

        return result;
    }

    /**
     * 配置异常通知
     *
     * @param joinPoint join point for advice
     * @param e         exception
     */
    @AfterThrowing(pointcut = "webLog()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        logger.error(ThrowableUtil.getStackTrace(e));
    }
}
