package csnight.redis.monitor.exception;

public class ConfigException extends Exception {
    public ConfigException() {
    }

    public ConfigException(String msg) {
        super("Redis Config Error:" + msg);
    }

    public ConfigException(String msg, Throwable t) {
        super( msg, t);
    }
}
