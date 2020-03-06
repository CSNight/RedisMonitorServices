package csnight.redis.monitor.monitor;

public interface RedisMonitor {
    MonitorState getState();

    void setSamples(double val);

    void setState(MonitorState state);

    void destroy();
}
