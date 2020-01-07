package csnight.redis.monitor.quartz;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MsCustomTask {
    private static Logger _log = LoggerFactory.getLogger(MsCustomTask.class);
    private ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    public void StartCustomTaskPool(Runnable runnable, long delay, long interval) {
        _log.info("Starting CustomTaskPool");
        scheduledExecutor.scheduleAtFixedRate(runnable, delay, interval, TimeUnit.MILLISECONDS);
        _log.info("CustomTaskPool Started!");
    }

    public void StopCustomTaskPool() {
        _log.info("Shutting down CustomTaskPool");
        try {
            scheduledExecutor.shutdown();
        } catch (Exception ex) {
            _log.error("CustomTaskPool stop failure cause by" + ex.getMessage() + ",Retry Force Shutdown");
            scheduledExecutor.shutdownNow();
            _log.info("CustomTaskPool Stopped!");
        } finally {
            System.gc();
        }
        _log.info("CustomTaskPool Stopped!");
    }
}
