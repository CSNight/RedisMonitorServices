package csnight.redis.monitor.aop;


import csnight.redis.monitor.busi.sys.OpLogServiceImpl;
import csnight.redis.monitor.db.jpa.SysOpLog;
import csnight.redis.monitor.utils.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LogAsyncPool {
    private static Logger _log = LoggerFactory.getLogger(LogAsyncPool.class);
    private OpLogServiceImpl opLogService;
    private static LogAsyncPool ourInstance;
    private ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);
    private ConcurrentLinkedQueue<SysOpLog> queue = new ConcurrentLinkedQueue<>();

    public static LogAsyncPool getIns() {
        if (ourInstance == null) {
            synchronized (LogAsyncPool.class) {
                if (ourInstance == null) {
                    ourInstance = new LogAsyncPool();
                }
            }
        }
        return ourInstance;
    }

    private LogAsyncPool() {

    }

    public void initBean() {
        opLogService = ReflectUtils.getBean(OpLogServiceImpl.class);
    }

    public boolean offer(SysOpLog ins) {
        return queue.offer(ins);
    }

    public void StartLogPool() {
        _log.info("Starting LogAsyncPool");
        scheduledThreadPool.scheduleAtFixedRate(() -> {
            if (!queue.isEmpty()) {
                List<SysOpLog> ins = new ArrayList<>();
                while (!queue.isEmpty()) {
                    ins.add(queue.poll());
                }
                if (ins.size() != 0) {
                    opLogService.SaveAll(ins);
                }
                ins.clear();
                ins = null;
            }
        }, 1000, 500, TimeUnit.MILLISECONDS);
        _log.info("LogAsyncPool Started!");
    }

    public void StopLogPool() {
        _log.info("Shutting down LogAsyncPool");
        try {
            scheduledThreadPool.shutdown();
        } catch (Exception ex) {
            _log.error("LogAsyncPool stop failure cause by" + ex.getMessage() + ",Retry Force Shutdown");
            scheduledThreadPool.shutdownNow();
            _log.info("LogAsyncPool Stopped!");
        }
        _log.info("LogAsyncPool Stopped!");
    }
}
