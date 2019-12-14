package csnight.redis.monitor.msg.series;

import com.csnight.jedisql.util.SafeEncoder;

import java.util.Arrays;

public enum ResponseMsgType {
    INIT(1),
    CONNECTED(2),
    DISCONNECTED(3),
    UNKNOWN(404),
    RESP(101),
    PUBSUB(102),
    DESUB(103),
    ERROR(400);

    private int code;
    private final byte[] raw = SafeEncoder.encode(this.name());

    ResponseMsgType(int code) {
        this.code = code;
    }

    public byte[] getRaw() {
        return this.raw;
    }

    public int getCode() {
        return this.code;
    }

    public static ResponseMsgType getEnumType(String key) {
        return Arrays.stream(ResponseMsgType.values())
                .filter(cmd -> cmd.name().equals(key))
                .findFirst().orElse(ResponseMsgType.UNKNOWN);
    }
}
