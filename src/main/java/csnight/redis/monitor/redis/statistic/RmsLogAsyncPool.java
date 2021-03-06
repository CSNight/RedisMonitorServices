package csnight.redis.monitor.redis.statistic;

import csnight.redis.monitor.db.jpa.RmsLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RmsLogAsyncPool {
    private static Logger _log = LoggerFactory.getLogger(RmsLogAsyncPool.class);
    public String executorConf = "mysql,file";
    public String es_addressed;
    private List<RmsLogsExecutor> executors = new ArrayList<>();
    private ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);
    private ScheduledExecutorService accessCheckPool = Executors.newScheduledThreadPool(1);
    private ConcurrentLinkedQueue<RmsLog> queue = new ConcurrentLinkedQueue<>();

    public void setExecutorConf(String executorConf) {
        this.executorConf = executorConf;
    }

    public void setEs_addressed(String es_addressed) {
        this.es_addressed = es_addressed;
    }

    public void initialize() {
        String[] execs = executorConf.split(",");
        for (String exec : execs) {
            if (exec.equals("mysql")) {
                executors.add(new MysqlRmsLogExecutorImpl());
            }
            if (exec.equals("file")) {
                executors.add(new FileRmsLogExecutorImpl());
            }
            if (exec.equals("elastic")) {
                executors.add(new ElasticRmsLogExecutorImpl(es_addressed));
            }
        }
        StartLogPool();
    }

    public boolean offer(RmsLog rmsLog) {
        return queue.offer(rmsLog);
    }

    public void StartLogPool() {
        _log.info("Starting RmsLogPool");
        scheduledThreadPool.scheduleAtFixedRate(this::execute, 1, 3, TimeUnit.SECONDS);
        accessCheckPool.scheduleAtFixedRate(this::checkExecutorsAccess, 1, 10, TimeUnit.SECONDS);
        _log.info("RmsLogPool Started!");
    }

    private void execute() {
        try {
            if (!queue.isEmpty()) {
                List<RmsLog> ins = new ArrayList<>();
                while (!queue.isEmpty()) {
                    ins.add(queue.poll());
                }
                if (ins.size() != 0) {
                    for (RmsLogsExecutor executor : executors) {
                        if (executor.isAccessible()) {
                            executor.execute(ins);
                        }
                    }
                }
                ins.clear();
                ins = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void checkExecutorsAccess() {
        for (RmsLogsExecutor executor : executors) {
            executor.checkAccess();
        }
    }

    public void destroy() {
        _log.info("Shutting down RmsLogs Pool...");
        try {
            execute();
            scheduledThreadPool.shutdown();
            executors.forEach(RmsLogsExecutor::destroy);
            _log.info("RmsLogs Pool Stopped!");
        } catch (Exception ex) {
            _log.error("RmsLogs Pool stop failure cause by " + ex.getMessage() + ", Retry Force Shutdown");
            scheduledThreadPool.shutdownNow();
            _log.info("RmsLogs Pool Stopped!");
        }
    }
}
