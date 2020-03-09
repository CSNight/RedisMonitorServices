package csnight.redis.monitor.context;

import csnight.redis.monitor.aop.LogAsyncPool;
import csnight.redis.monitor.monitor.MonitorBus;
import csnight.redis.monitor.msg.MsgBus;
import csnight.redis.monitor.quartz.JobFactory;
import csnight.redis.monitor.redis.pool.MultiRedisPool;
import csnight.redis.monitor.utils.ReflectUtils;
import csnight.redis.monitor.utils.YamlUtils;
import csnight.redis.monitor.websocket.WebSocketServer;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

@Component
public class SpringContextEvent implements ApplicationListener<ApplicationEvent> {
    private static Logger _log = LoggerFactory.getLogger(SpringContextEvent.class);
    private LogAsyncPool logAsyncPool = LogAsyncPool.getIns();
    private WebSocketServer wss = WebSocketServer.getInstance();

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof ApplicationStartedEvent) {
            wss.setHost(YamlUtils.getStrYmlVal("websocket.server"));
            wss.setPort(YamlUtils.getIntegerYmlVal("websocket.port"));
            try {
                wss.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
            logAsyncPool.initBean();
            logAsyncPool.StartLogPool();
            _log.info("RMS Server Start Complete!");
            System.gc();
        } else if (applicationEvent instanceof ContextClosedEvent) {
            logAsyncPool.StopLogPool();
            MsgBus.getIns().removeAll();
            wss.shutdown();
            try {
                ReflectUtils.getBean(JobFactory.class).PauseAllJob();
                ReflectUtils.getBean(SchedulerFactoryBean.class).getScheduler().shutdown(true);
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
            MonitorBus.getIns().destroy();
            _log.info("Shutting down Redis pools");
            MultiRedisPool.getInstance().shutdown();
            _log.info("All Redis pools have stopped!");
            _log.info("RMS Server Stop Complete!");
        }
    }
}

