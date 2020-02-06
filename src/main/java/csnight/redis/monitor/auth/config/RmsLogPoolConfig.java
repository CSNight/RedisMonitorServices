package csnight.redis.monitor.auth.config;

import csnight.redis.monitor.redis.statistic.RmsLogAsyncPool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RmsLogPoolConfig {
    @Value("${rms-log.executors}")
    public String executorConf;

    @Bean
    public RmsLogAsyncPool initializePool() {
        RmsLogAsyncPool rmsLogAsyncPool = new RmsLogAsyncPool();
        rmsLogAsyncPool.setExecutorConf(executorConf);
        rmsLogAsyncPool.initializeExecutors();
        return rmsLogAsyncPool;
    }
}
