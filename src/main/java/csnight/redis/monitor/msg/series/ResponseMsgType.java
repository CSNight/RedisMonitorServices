package csnight.redis.monitor.msg.series;

import com.csnight.jedisql.util.SafeEncoder;

import java.util.Arrays;

public enum ResponseMsgType {
    INIT(1),
    UNKNOWN(404),

    RESP(101),
    SUBCON(102),
    PUBSUB(103),
    DESUB(104),
    MONITORCON(105),
    MONITOR(106),
    DEMONITOR(107),

    KEYWATCHCON(200),
    KEYWATCH(201),
    DEKEYWATCH(202),
    SHAKESTART(203),
    SHAKEPROCESS(204),
    SHAKEFINISH(205),

    PHYSICAL_STARTED(1000),
    PHYSICAL_STOPPED(1001),
    PHYSICAL_STAT(1002),
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
