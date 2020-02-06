package csnight.redis.monitor.redis.statistic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileRmsLogExecutorImpl implements RmsLogsExecutor {
    private Logger _log = LoggerFactory.getLogger(FileRmsLogExecutorImpl.class);

    public FileRmsLogExecutorImpl() {
        _log.info("File redis statistic log executors initialize");
    }
}
