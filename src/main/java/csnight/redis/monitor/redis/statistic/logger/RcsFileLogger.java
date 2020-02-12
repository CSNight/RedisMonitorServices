package csnight.redis.monitor.redis.statistic.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RcsFileLogger {
    private Logger _log = LoggerFactory.getLogger(RcsFileLogger.class);

    public void log(String body) {
        _log.info(body);
    }
}
