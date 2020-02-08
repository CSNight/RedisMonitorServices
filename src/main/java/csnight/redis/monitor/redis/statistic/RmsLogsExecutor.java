package csnight.redis.monitor.redis.statistic;

import csnight.redis.monitor.db.jpa.RmsLog;

import java.util.List;

public interface RmsLogsExecutor {
    boolean execute(List<RmsLog> logs);

    boolean isAccessible();

    void checkAccess();

    boolean destroy();
}
