package csnight.redis.monitor.msg.handler;

import com.csnight.jedisql.JediSQL;
import csnight.redis.monitor.msg.entity.PubSubEntity;
import csnight.redis.monitor.msg.series.CmdMsgType;
import csnight.redis.monitor.redis.pool.MultiRedisPool;
import csnight.redis.monitor.redis.pool.RedisPoolInstance;
import io.netty.channel.Channel;

public class SubscribeHandler implements WsChannelHandler {
    private PubSubEntity pubSubEntity = new PubSubEntity();
    private Channel channel;
    private RedisPoolInstance pool;
    private String appId;
    private JediSQL jediSQL;
    private CmdMsgType t;

    public SubscribeHandler(String appId, Channel ch, String ins, CmdMsgType t) {
        this.appId = appId;
        this.channel = ch;
        this.t = t;
        this.pool = MultiRedisPool.getInstance().getPool(ins);
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void setPool(RedisPoolInstance pool) {
        this.pool = pool;
    }

    public PubSubEntity getPubSubEntity() {
        return pubSubEntity;
    }

    public String getAppId() {
        return appId;
    }

    @Override
    public void initialize() {
        pubSubEntity.setCh(channel);
        jediSQL = pool.getJedis(pubSubEntity.getId());
        pubSubEntity.setJediSQL(jediSQL);
    }

    public void startSubscribe(String... params) {
        Thread thread = new Thread(() -> {
            if (t.equals(CmdMsgType.SUB)) {
                pubSubEntity.subscribe(params);
            } else {
                pubSubEntity.psubscribe(params);
            }
        });
        thread.start();
    }

    public void stopSubscribe(String... params) {
        try {
            if (t.equals(CmdMsgType.SUB)) {
                pubSubEntity.unsubscribe(params);
            } else {
                pubSubEntity.punsubscribe(params);
            }
        } finally {
            pool.close(pubSubEntity.getId());
            pubSubEntity = null;
        }
    }


    @Override
    public void destroy() {
        if (pool != null) {
            stopSubscribe();
            jediSQL = null;
        }
        pubSubEntity = null;
    }
}
