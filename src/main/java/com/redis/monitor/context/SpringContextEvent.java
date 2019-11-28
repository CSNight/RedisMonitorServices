package com.redis.monitor.context;

import com.redis.monitor.aop.LogAsyncPool;
import com.redis.monitor.quartz.JobFactory;
import com.redis.monitor.utils.ReflectUtils;
import com.redis.monitor.websocket.WebSocketServerSingleton;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.ServletRequestHandledEvent;

@Component
public class SpringContextEvent implements ApplicationListener {
    private static Logger _log = LoggerFactory.getLogger(SpringContextEvent.class);
    private LogAsyncPool logAsyncPool = LogAsyncPool.getIns();
    private WebSocketServerSingleton wss = WebSocketServerSingleton.getInstance();

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof ApplicationStartedEvent) {
            wss.setHost("127.0.0.1");
            wss.setPort(13244);
            try {
                wss.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
            logAsyncPool.initBean();
            logAsyncPool.StartLogPool();
            _log.info("RMS Server Start Complete!");
        } else if (applicationEvent instanceof ContextClosedEvent) {
            logAsyncPool.StopLogPool();
            wss.shutdown();
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

