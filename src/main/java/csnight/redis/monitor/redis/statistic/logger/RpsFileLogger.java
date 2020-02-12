package csnight.redis.monitor.redis.statistic.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpsFileLogger {
    private Logger _log = LoggerFactory.getLogger(RpsFileLogger.class);

    public void log(String body) {
        _log.info(body);
    }
}
