package csnight.redis.monitor.redis.statistic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticRmsLogExecutorImpl implements RmsLogsExecutor {
    private Logger _log = LoggerFactory.getLogger(ElasticRmsLogExecutorImpl.class);

    public ElasticRmsLogExecutorImpl() {
        _log.info("Elasticsearch redis statistic log executors initialize");
    }
}
