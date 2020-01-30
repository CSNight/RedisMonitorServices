package csnight.redis.monitor.msg.series;

import com.csnight.jedisql.util.SafeEncoder;

import java.util.Arrays;

public enum StatMsgType {
    PHYSICAL_START(100),
    PHYSICAL_STOP(101),
    UNKNOWN(-1);
    private int ct;
    private final byte[] raw = SafeEncoder.encode(this.name());

    StatMsgType(int ct) {
        this.ct = ct;
    }

    public byte[] getRaw() {
        return this.raw;
    }

    public int getCt() {
        return ct;
    }

    public static StatMsgType getEnumType(String key) {
        return Arrays.stream(StatMsgType.values())
                .filter(cmd -> cmd.name().equals(key))
                .findFirst().orElse(StatMsgType.UNKNOWN);
    }

    public static StatMsgType getEnumType(int key) {
        return Arrays.stream(StatMsgType.values())
                .filter(cmd -> cmd.ct == key)
                .findFirst().orElse(StatMsgType.UNKNOWN);
    }
}
