package csnight.redis.monitor.monitor;

public interface RedisMonitor {
    MonitorState getState();

    void setSamples(double val);

    void setState(MonitorState state);

    int getDelay();

    void setDelay(int delay);

    void destroy();
}
