package csnight.redis.monitor.redis.statistic;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FileRmsLogExecutorImpl implements RmsLogsExecutor {
    private Logger _log = LoggerFactory.getLogger(FileRmsLogExecutorImpl.class);
    private boolean isAccessible = true;

    public FileRmsLogExecutorImpl() {
        _log.info("File redis statistic log executors initialize");
    }

    @Override
    public boolean isAccessible() {
        return isAccessible;
    }

    @Override
    public void checkAccess() {
        isAccessible = true;
    }

    @Override
    public boolean execute(List<RmsLog> logs) {
        logs.forEach(log -> System.out.println("File--" + JSONObject.toJSON(log)));
        return false;
    }

    @Override
    public boolean destroy() {
        return true;
    }
}
