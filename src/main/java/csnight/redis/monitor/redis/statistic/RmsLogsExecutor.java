package csnight.redis.monitor.redis.statistic;

import java.util.List;

public interface RmsLogsExecutor {
    boolean execute(List<RmsLog> logs);

    boolean isAccessible();

    void checkAccess();

    boolean destroy();
}
