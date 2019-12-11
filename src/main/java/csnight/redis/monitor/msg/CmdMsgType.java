package csnight.redis.monitor.msg;

import com.csnight.jedisql.util.SafeEncoder;

import java.util.Arrays;

public enum CmdMsgType {
    UNKNOWN(-1),
    CMD(100),
    PUB(200),
    SUB(201),
    DESUB(202);
    private int ct;
    private final byte[] raw = SafeEncoder.encode(this.name());

    CmdMsgType(int ct) {
        this.ct = ct;
    }

    public byte[] getRaw() {
        return this.raw;
    }

    public int getCt() {
        return ct;
    }

    public static CmdMsgType getEnumType(String key) {
        return Arrays.stream(CmdMsgType.values())
                .filter(cmd -> cmd.name().equals(key))
                .findFirst().orElse(CmdMsgType.UNKNOWN);
    }

    public static CmdMsgType getEnumType(int key) {
        return Arrays.stream(CmdMsgType.values())
                .filter(cmd -> cmd.ct == key)
                .findFirst().orElse(CmdMsgType.UNKNOWN);
    }
}
