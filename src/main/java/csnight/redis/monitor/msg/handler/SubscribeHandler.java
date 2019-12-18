package csnight.redis.monitor.msg.handler;

import com.alibaba.fastjson.JSONObject;
import com.csnight.jedisql.JediSQL;
import csnight.redis.monitor.exception.CmdMsgException;
import csnight.redis.monitor.msg.entity.PubSubEntity;
import csnight.redis.monitor.msg.entity.WssResponseEntity;
import csnight.redis.monitor.msg.series.CmdMsgType;
import csnight.redis.monitor.msg.series.ResponseMsgType;
import csnight.redis.monitor.redis.pool.MultiRedisPool;
import csnight.redis.monitor.redis.pool.RedisPoolInstance;
import csnight.redis.monitor.websocket.WebSocketServer;
import io.netty.channel.Channel;

public class SubscribeHandler implements WsChannelHandler {
    private PubSubEntity pubSubEntity = new PubSubEntity();
    private Channel channel;
    private RedisPoolInstance pool;
    private String appId;
    private JediSQL jediSQL;
    private CmdMsgType t;
    private String[] params;
    private Thread thread;

    public SubscribeHandler(String appId, Channel ch, CmdMsgType t) {
        this.appId = appId;
        this.channel = ch;
        this.t = t;
    }

    public void setPool(RedisPoolInstance pool) {
        this.pool = pool;
    }

    @Override
    public void initialize(JSONObject msg) throws CmdMsgException {
        String ins = msg.getString("ins");
        if (ins == null || ins.equals("")) {
            throw new CmdMsgException("Must specify a redis instance");
        }
        pool = MultiRedisPool.getInstance().getPool(ins);
        if (pool == null) {
            throw new CmdMsgException("Redis pool does not exist, please connect first");
        }
        String msgBody = msg.getString("msg");
        parseParams(msgBody);
        pubSubEntity.setCh(channel);
        pubSubEntity.setAppId(appId);
        jediSQL = pool.getJedis(pubSubEntity.getId());
        pubSubEntity.setJediSQL(jediSQL);
    }

    private void parseParams(String msg) throws CmdMsgException {
        String[] parts = msg.split(" ");
        if (parts.length < 2) {
            throw new CmdMsgException("Empty channels or patterns");
        }
        params = new String[parts.length - 1];
        System.arraycopy(parts, 1, params, 0, parts.length - 1);
    }

    public void startSubscribe() {
        thread = new Thread(() -> {
            try {
                if (t.equals(CmdMsgType.SUB)) {
                    pubSubEntity.subscribe(params);
                } else {
                    pubSubEntity.psubscribe(params);
                }
            } catch (Exception ex) {
                //连接断开线程直接退出，发送错误消息及断开消息
                WssResponseEntity wre = new WssResponseEntity(ResponseMsgType.ERROR, "Connection broken");
                wre.setAppId(appId);
                WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), channel);
                WssResponseEntity wres = new WssResponseEntity(ResponseMsgType.DESUB, "Unsubscribe success");
                wres.setAppId(appId);
                WebSocketServer.getInstance().send(JSONObject.toJSONString(wres), channel);
            }
        });
        thread.start();
    }

    private void stopSubscribe() {
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
        System.out.println(thread.isAlive());
        pubSubEntity = null;
    }
}
