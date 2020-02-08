package csnight.redis.monitor.auth.config;

import csnight.redis.monitor.redis.statistic.RmsLogAsyncPool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RmsLogPoolConfig {
    @Value("${rms-log.executors}")
    public String executorConf;
    @Value("${rms-log.elastic.addresses}")
    public String es_addresses;

    @Bean(initMethod = "initialize", destroyMethod = "destroy")
    public RmsLogAsyncPool initializePool() {
        RmsLogAsyncPool rmsLogAsyncPool = new RmsLogAsyncPool();
        rmsLogAsyncPool.setExecutorConf(executorConf);
        if (es_addresses != null && executorConf.contains("elastic")) {
            rmsLogAsyncPool.setEs_addressed(es_addresses);
        }
        return rmsLogAsyncPool;
    }
}
