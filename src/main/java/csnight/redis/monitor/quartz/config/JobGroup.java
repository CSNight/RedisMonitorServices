package csnight.redis.monitor.quartz.config;

public enum JobGroup {
    STATISTIC(1),
    CUSTOM(100);
    private int ct;

    JobGroup(int ct) {
        this.ct = ct;
    }

}
