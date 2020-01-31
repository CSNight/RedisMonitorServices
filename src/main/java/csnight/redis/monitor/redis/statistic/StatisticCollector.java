package csnight.redis.monitor.redis.statistic;

import csnight.redis.monitor.redis.pool.RedisPoolInstance;
import io.netty.channel.Channel;

public class StatisticCollector {
    private String appId;
    private RedisPoolInstance pool;
    private String ins_id;
    private Channel channel;


    public StatisticCollector(String ins_id) {
        this.ins_id = ins_id;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public RedisPoolInstance getPool() {
        return pool;
    }

    public void setPool(RedisPoolInstance pool) {
        this.pool = pool;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getIns_id() {
        return ins_id;
    }


    public void execute() {

    }
}
