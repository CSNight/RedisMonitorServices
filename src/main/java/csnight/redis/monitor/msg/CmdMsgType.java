package csnight.redis.monitor.msg;

import com.csnight.jedisql.util.SafeEncoder;

import java.util.Arrays;

public enum CmdMsgType {
    UNKNOWN(-1),
    CONNECT(100),
    DISCONNECT(101),
    CMD(200),
    PUB(300),
    SUB(301),
    DESUB(302);
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
