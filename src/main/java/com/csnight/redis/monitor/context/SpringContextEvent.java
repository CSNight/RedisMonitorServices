package com.csnight.redis.monitor.context;

import com.csnight.redis.monitor.quartz.JobFactory;
import com.csnight.redis.monitor.utils.ReflectUtils;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.support.ServletRequestHandledEvent;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SpringContextEvent implements ApplicationListener {
    private static Logger _log = LoggerFactory.getLogger(SpringContextEvent.class);

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof ApplicationStartedEvent) {
            System.out.println("Start Complete!");
        } else if (applicationEvent instanceof ContextClosedEvent) {
            ReflectUtils.getBean(JobFactory.class).DeleteAllJob();
            try {
                ReflectUtils.getBean(SchedulerFactoryBean.class).getScheduler().shutdown(true);
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        } else if (applicationEvent instanceof ServletRequestHandledEvent) {
            ServletRequestHandledEvent rhe = (ServletRequestHandledEvent) applicationEvent;
            if (rhe.getStatusCode() != 200) {
                _log.info(rhe.getClientAddress() + " " + rhe.getMethod() + " " + rhe.getStatusCode() + " " + rhe.getRequestUrl());
            }
        }
    }
}

