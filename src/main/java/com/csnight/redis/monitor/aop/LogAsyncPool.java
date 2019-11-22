package com.csnight.redis.monitor.aop;


import com.csnight.redis.monitor.busi.sys.OpLogServiceImpl;
import com.csnight.redis.monitor.db.repos.SysLogRepository;
import com.csnight.redis.monitor.utils.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LogAsyncPool {
    private static Logger _log = LoggerFactory.getLogger(LogAsyncPool.class);
    private SysLogRepository sysLogRepository;
    private static LogAsyncPool ourInstance;
    private ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(2);
    private ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();

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
        sysLogRepository = ReflectUtils.getBean(OpLogServiceImpl.class).getSysLogRepository();
    }

    public boolean offer(String ins) {
        return queue.offer(ins);
    }

    public void StartLogPool() {
        _log.info("Starting LogAsyncPool");
        scheduledThreadPool.scheduleAtFixedRate(() -> {
            if (!queue.isEmpty()) {

            }
        }, 1000, 100, TimeUnit.MILLISECONDS);
        _log.info("LogAsyncPool Started");
    }

    public void StopLogPool() {
        _log.info("Shutting down LogAsyncPool");
        try {
            scheduledThreadPool.shutdown();
        } catch (Exception ex) {
            _log.error("LogAsyncPool Stopped Failure,Retry Force Shutdown");
            ex.printStackTrace();
            scheduledThreadPool.shutdownNow();
            _log.error("LogAsyncPool Stopped");
        }
        _log.info("LogAsyncPool Stopped");
    }


}
