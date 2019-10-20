package com.csnight.redis.monitor.aop;

import com.csnight.redis.monitor.utils.ThrowableUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.autoconfigure.web.servlet.error.DefaultErrorViewResolver;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Component
@Aspect
@Order(2)
public class LogAspectAnnotation {
    private Logger logger = LoggerFactory.getLogger(LogAspectAnnotation.class);

    private ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Pointcut("@annotation(com.csnight.redis.monitor.aop.LogBack)")
    public void webLog() {
    }

    @Around("webLog()")
    public Object logHandler(ProceedingJoinPoint process) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) process.getSignature();
        Method method = methodSignature.getMethod();
        String methodName = method.getName();
        String className = method.getDeclaringClass().getName();
        Object[] args = process.getArgs();
        StringBuilder params = new StringBuilder();
        for (Object arg : args) {
            params.append(arg.toString());
            params.append(";");
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
        }
        HttpServletResponse rep = attributes.getResponse();
        long costTime = System.currentTimeMillis() - startTime.get();
        assert rep != null;
        logger.info("\r\nCost:{}ms Status:" + rep.getStatus() +
                "\r\nClass:{}=>{}\r\n" +
                "Response => {}", costTime, className, methodName, result);
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
