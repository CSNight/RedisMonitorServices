package csnight.redis.monitor.redis.statistic;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ElasticRmsLogExecutorImpl implements RmsLogsExecutor {
    private Logger _log = LoggerFactory.getLogger(ElasticRmsLogExecutorImpl.class);
    private boolean isAccessible = true;

    public ElasticRmsLogExecutorImpl() {
        _log.info("Elasticsearch redis statistic log executors initialize");
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
        logs.forEach(log -> System.out.println("ES--" + JSONObject.toJSON(log)));
        return false;
    }
}
