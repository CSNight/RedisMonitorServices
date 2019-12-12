package csnight.redis.monitor.msg.series;

import com.csnight.jedisql.commands.ProtocolCommand;
import com.csnight.jedisql.util.SafeEncoder;

import java.util.Arrays;

public enum RedisKeyType implements ProtocolCommand {
    AGGREGATE,
    ALPHA,
    ASC,
    BY,
    DESC,
    GET,
    LIMIT,
    MESSAGE,
    NO,
    NOSORT,
    PMESSAGE,
    PSUBSCRIBE,
    PUNSUBSCRIBE,
    OK,
    ONE,
    QUEUED,
    SET,
    STORE,
    SUBSCRIBE,
    UNSUBSCRIBE,
    WEIGHTS,
    WITHSCORES,
    RESETSTAT,
    REWRITE,
    RESET,
    FLUSH,
    EXISTS,
    LOAD,
    KILL,
    LEN,
    REFCOUNT,
    ENCODING,
    IDLETIME,
    GETNAME,
    SETNAME,
    LIST,
    MATCH,
    COUNT,
    PING,
    PONG,
    UNLOAD,
    REPLACE,
    KEYS,
    PAUSE,
    DOCTOR,
    BLOCK,
    NOACK,
    STREAMS,
    KEY,
    CREATE,
    MKSTREAM,
    SETID,
    DESTROY,
    DELCONSUMER,
    MAXLEN,
    GROUP,
    IDLE,
    TIME,
    RETRYCOUNT,
    FORCE,
    UNKNOWN;

    private final byte[] raw = SafeEncoder.encode(this.name());

    RedisKeyType() {
    }

    public byte[] getRaw() {
        return this.raw;
    }

    public static RedisKeyType getEnumType(String key) {
        return Arrays.stream(RedisKeyType.values())
                .filter(cmd -> cmd.name().equals(key))
                .findFirst().orElse(RedisKeyType.UNKNOWN);
    }
}
