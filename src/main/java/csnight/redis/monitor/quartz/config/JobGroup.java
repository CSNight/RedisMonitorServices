package csnight.redis.monitor.quartz.config;

import com.csnight.jedisql.util.SafeEncoder;

import java.util.Arrays;

public enum JobGroup {
    UNKNOWN(-1),
    STATISTIC(1),
    EXECUTION(2),
    REPORTER(3),
    ERROR(400);
    private final byte[] raw = SafeEncoder.encode(this.name());
    private int ct;

    JobGroup(int ct) {
        this.ct = ct;
    }

    public byte[] getRaw() {
        return this.raw;
    }

    public int getCt() {
        return ct;
    }

    public static JobGroup getEnumType(String key) {
        return Arrays.stream(JobGroup.values())
                .filter(cmd -> cmd.name().equals(key))
                .findFirst().orElse(JobGroup.UNKNOWN);
    }

    public static JobGroup getEnumType(int key) {
        return Arrays.stream(JobGroup.values())
                .filter(cmd -> cmd.ct == key)
                .findFirst().orElse(JobGroup.UNKNOWN);
    }
}
