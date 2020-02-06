package csnight.redis.monitor.redis.statistic;

import java.util.ArrayList;
import java.util.List;

public class RmsLogAsyncPool {
    public String executorConf;
    private List<RmsLogsExecutor> executors = new ArrayList<>();

    public void setExecutorConf(String executorConf) {
        this.executorConf = executorConf;
    }

    public String getExecutorConf() {
        return executorConf;
    }

    public void initializeExecutors() {
        String[] execs = executorConf.split(",");
        for (String exec : execs) {
            if (exec.equals("mysql")) {
                executors.add(new MysqlRmsLogExecutorImpl());
            }
            if (exec.equals("file")) {
                executors.add(new FileRmsLogExecutorImpl());
            }
            if (exec.equals("elastic")) {
                executors.add(new ElasticRmsLogExecutorImpl());
            }
        }
    }
}
