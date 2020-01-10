package csnight.redis.monitor.msg.series;

import com.csnight.jedisql.util.SafeEncoder;

import java.util.Arrays;

public enum DataMsgType {
    KEYWATCH(100),
    UNKEYWATCH(101),
    UNKNOWN(-1);
    private int ct;
    private final byte[] raw = SafeEncoder.encode(this.name());

    DataMsgType(int ct) {
        this.ct = ct;
    }

    public byte[] getRaw() {
        return this.raw;
    }

    public int getCt() {
        return ct;
    }

    public static DataMsgType getEnumType(String key) {
        return Arrays.stream(DataMsgType.values())
                .filter(cmd -> cmd.name().equals(key))
                .findFirst().orElse(DataMsgType.UNKNOWN);
    }

    public static DataMsgType getEnumType(int key) {
        return Arrays.stream(DataMsgType.values())
                .filter(cmd -> cmd.ct == key)
                .findFirst().orElse(DataMsgType.UNKNOWN);
    }
}
