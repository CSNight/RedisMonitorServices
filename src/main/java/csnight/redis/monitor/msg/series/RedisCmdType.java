package csnight.redis.monitor.msg.series;

import com.csnight.jedisql.commands.ProtocolCommand;
import com.csnight.jedisql.util.SafeEncoder;

import java.util.Arrays;

public enum RedisCmdType implements ProtocolCommand {
    APPEND,
    ASKING,
    AUTH,
    BGREWRITEAOF,
    BGSAVE,
    BITCOUNT,
    BITFIELD,
    BITOP,
    BITPOS,
    BLPOP,
    BRPOP,
    BRPOPLPUSH,
    CLIENT,
    CLUSTER,
    CONFIG,
    COMMAND,
    DBSIZE,
    DEBUG,
    DECR,
    DECRBY,
    DEL,
    DISCARD,
    DUMP,
    ECHO,
    EVAL,
    EVALSHA,
    EXEC,
    EXISTS,
    EXPIRE,
    EXPIREAT,
    FLUSHALL,
    FLUSHDB,
    GEOADD,
    GEODIST,
    GEOHASH,
    GEOPOS,
    GEORADIUS,
    GEORADIUSBYMEMBER,
    GEORADIUSBYMEMBER_RO,
    GEORADIUS_RO,
    GET,
    GETBIT,
    GETRANGE,
    GETSET,
    HDEL,
    HEXISTS,
    HGET,
    HGETALL,
    HINCRBY,
    HINCRBYFLOAT,
    HKEYS,
    HLEN,
    HMGET,
    HMSET,
    HSCAN,
    HSET,
    HSETNX,
    HSTRLEN,
    HVALS,
    INCR,
    INCRBY,
    INCRBYFLOAT,
    INFO,
    KEYS,
    LASTSAVE,
    LINDEX,
    LINSERT,
    LLEN,
    LPOP,
    LPUSH,
    LPUSHX,
    LRANGE,
    LREM,
    LSET,
    LTRIM,
    MEMORY,
    MGET,
    MIGRATE,
    MODULE,
    MONITOR,
    MOVE,
    MSET,
    MSETNX,
    MULTI,
    OBJECT,
    PERSIST,
    PEXPIRE,
    PEXPIREAT,
    PFADD,
    PFCOUNT,
    PFMERGE,
    PING,
    PSETEX,
    PSUBSCRIBE,
    PTTL,
    PUBLISH,
    PUBSUB,
    PUNSUBSCRIBE,
    QUIT,
    RANDOMKEY,
    READONLY,
    RENAME,
    RENAMENX,
    RENAMEX,
    RESTORE,
    RPOP,
    RPOPLPUSH,
    RPUSH,
    RPUSHX,
    SADD,
    SAVE,
    SCAN,
    SCARD,
    SCRIPT,
    SDIFF,
    SDIFFSTORE,
    SELECT,
    SENTINEL,
    SET,
    SETBIT,
    SETEX,
    SETNX,
    SETRANGE,
    SHUTDOWN,
    SINTER,
    SINTERSTORE,
    SISMEMBER,
    SLAVEOF,
    SLOWLOG,
    SMEMBERS,
    SMOVE,
    SORT,
    SPOP,
    SRANDMEMBER,
    SREM,
    SSCAN,
    STRLEN,
    SUBSCRIBE,
    SUBSTR,
    SUNION,
    SUNIONSTORE,
    SWAPDB,
    SYNC,
    TIME,
    TOUCH,
    TTL,
    TYPE,
    UNLINK,
    UNSUBSCRIBE,
    UNWATCH,
    WAIT,
    WATCH,
    XACK,
    XADD,
    XCLAIM,
    XDEL,
    XGROUP,
    XLEN,
    XPENDING,
    XRANGE,
    XREAD,
    XREADGROUP,
    XREVRANGE,
    XTRIM,
    ZADD,
    ZCARD,
    ZCOUNT,
    ZINCRBY,
    ZINTERSTORE,
    ZLEXCOUNT,
    ZPOPMIN,
    ZRANGE,
    ZRANGEBYLEX,
    ZRANGEBYSCORE,
    ZRANK,
    ZREM,
    ZREMRANGEBYLEX,
    ZREMRANGEBYRANK,
    ZREMRANGEBYSCORE,
    ZREVRANGE,
    ZREVRANGEBYLEX,
    ZREVRANGEBYSCORE,
    ZREVRANK,
    ZSCAN,
    ZSCORE,
    ZUNIONSTORE,
    BZPOPMAX,
    BZPOPMIN,
    HOST$,
    LATENCY,
    LOLWUT,
    PFDEBUG,
    PFSELFTEST,
    POST,
    PSYNC,
    READWRITE,
    REPLCONF,
    REPLICAOF,
    RESTORE_ASKING,
    ROLE,
    XINFO,
    XSETID,
    ZPOPMAX,
    UNKNOWN;

    private final byte[] raw;

    RedisCmdType() {
        if (this.name().contains("_")) {
            raw = SafeEncoder.encode(this.name().replace("_", "-"));
        } else if (this.name().contains("$")) {
            raw = SafeEncoder.encode(this.name().replace("$", ":"));
        } else {
            raw = SafeEncoder.encode(this.name());
        }
    }

    public byte[] getRaw() {
        return this.raw;
    }

    public static RedisCmdType getEnumType(String key) {
        return Arrays.stream(RedisCmdType.values())
                .filter(cmd -> new String(cmd.raw).equals(key))
                .findFirst().orElse(RedisCmdType.UNKNOWN);
    }

    public static String toCommands() {
        StringBuilder commands = new StringBuilder();
        for (RedisCmdType cmd : RedisCmdType.values()) {
            commands.append(cmd.name()).append(",");
        }
        return commands.toString().substring(0, commands.lastIndexOf(","));
    }
}
