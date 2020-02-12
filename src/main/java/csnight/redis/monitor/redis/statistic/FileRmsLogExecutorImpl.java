package csnight.redis.monitor.redis.statistic;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.db.jpa.RmsLog;
import csnight.redis.monitor.redis.statistic.logger.RcsFileLogger;
import csnight.redis.monitor.redis.statistic.logger.RksFileLogger;
import csnight.redis.monitor.redis.statistic.logger.RosFileLogger;
import csnight.redis.monitor.redis.statistic.logger.RpsFileLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FileRmsLogExecutorImpl implements RmsLogsExecutor {
    private Logger _log = LoggerFactory.getLogger(FileRmsLogExecutorImpl.class);
    private boolean isAccessible = true;
    private RcsFileLogger rcsFileLogger;
    private RosFileLogger rosFileLogger;
    private RpsFileLogger rpsFileLogger;
    private RksFileLogger rksFileLogger;

    public FileRmsLogExecutorImpl() {
        rcsFileLogger = new RcsFileLogger();
        rosFileLogger = new RosFileLogger();
        rpsFileLogger = new RpsFileLogger();
        rksFileLogger = new RksFileLogger();
        _log.info("File redis statistic log executors initialize success");
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
        for (RmsLog log : logs) {
            switch (log.getSector()) {
                case "Physical":
                    rpsFileLogger.log(JSONObject.toJSONString(log));
                    break;
                case "Commands":
                    rosFileLogger.log(JSONObject.toJSONString(log));
                    break;
                case "Clients":
                    rcsFileLogger.log(JSONObject.toJSONString(log));
                    break;
                case "Keyspace":
                    rksFileLogger.log(JSONObject.toJSONString(log));
                    break;
            }
        }
        return true;
    }

    @Override
    public boolean destroy() {
        return true;
    }
}
