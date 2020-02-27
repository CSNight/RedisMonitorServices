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

    STAT_STARTED(1000),
    STAT_STOPPED(1001),
    RMS_STAT(1002),

    EXEC_STARTED(1003),
    EXEC_STOPPED(1004),
    EXEC_ADDED(1005),
    EXEC_REMOVED(1006),
    RMS_EXEC(1007),

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
