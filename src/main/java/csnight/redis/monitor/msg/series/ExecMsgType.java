package csnight.redis.monitor.msg.series;

import com.csnight.jedisql.util.SafeEncoder;

import java.util.Arrays;

public enum ExecMsgType {
    EXEC_START(100),
    EXEC_STOP(101),
    EXEC_ADD(102),
    EXEC_REMOVE(103),
    UNKNOWN(-1);
    private int ct;
    private final byte[] raw = SafeEncoder.encode(this.name());

    ExecMsgType(int ct) {
        this.ct = ct;
    }

    public byte[] getRaw() {
        return this.raw;
    }

    public int getCt() {
        return ct;
    }

    public static ExecMsgType getEnumType(String key) {
        return Arrays.stream(ExecMsgType.values())
                .filter(cmd -> cmd.name().equals(key))
                .findFirst().orElse(ExecMsgType.UNKNOWN);
    }

    public static ExecMsgType getEnumType(int key) {
        return Arrays.stream(ExecMsgType.values())
                .filter(cmd -> cmd.ct == key)
                .findFirst().orElse(ExecMsgType.UNKNOWN);
    }
}
