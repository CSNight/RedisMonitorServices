package com.csnight.redis.monitor.context;

import com.csnight.redis.monitor.quartz.JobFactory;
import com.csnight.redis.monitor.utils.ReflectUtils;
import org.quartz.SchedulerException;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

public class SpringContextEvent implements ApplicationListener {

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
        }
    }
}

