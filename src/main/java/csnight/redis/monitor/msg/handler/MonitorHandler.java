package csnight.redis.monitor.msg.handler;

import com.alibaba.fastjson.JSONObject;
import com.csnight.jedisql.JediSQL;
import csnight.redis.monitor.exception.CmdMsgException;
import csnight.redis.monitor.msg.entity.JMonitorEntity;
import csnight.redis.monitor.msg.entity.WssResponseEntity;
import csnight.redis.monitor.msg.series.ResponseMsgType;
import csnight.redis.monitor.redis.pool.MultiRedisPool;
import csnight.redis.monitor.redis.pool.RedisPoolInstance;
import csnight.redis.monitor.websocket.WebSocketServer;
import io.netty.channel.Channel;

public class MonitorHandler implements WsChannelHandler {
    private JMonitorEntity jMonitorEntity = new JMonitorEntity();
    private Channel channel;
    private RedisPoolInstance pool;
    private String appId;
    private JediSQL jediSQL;
    private String ins;

    public MonitorHandler(String appId, Channel ch) {
        this.appId = appId;
        this.channel = ch;
    }

    @Override
    public String getIns() {
        return ins;
    }

    @Override
    public void initialize(JSONObject msg) throws CmdMsgException {
        ins = msg.getString("ins");
        if (ins == null || ins.equals("")) {
            throw new CmdMsgException("Must specify a redis instance");
        }
        pool = MultiRedisPool.getInstance().getPool(ins);
        if (pool == null) {
            throw new CmdMsgException("Redis pool does not exist, please connect first");
        }
        jMonitorEntity.setCh(channel);
        jMonitorEntity.setAppId(appId);
        jediSQL = pool.getJedis(jMonitorEntity.getId());
        jMonitorEntity.setJediSQL(jediSQL);
    }

    public void startMonitor() {
        Thread thread = new Thread(() -> {
            try {
                jMonitorEntity.jmonitor();
            } catch (Exception ex) {
                //连接断开线程直接退出，发送错误消息及断开消息
                WssResponseEntity wre = new WssResponseEntity(ResponseMsgType.ERROR, "Monitor stop with an error " + ex.getMessage());
                wre.setAppId(appId);
                WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), channel);
                WssResponseEntity wres = new WssResponseEntity(ResponseMsgType.DEMONITOR, "Unsubscribe success");
                wres.setAppId(appId);
                WebSocketServer.getInstance().send(JSONObject.toJSONString(wres), channel);
            }
        });
        thread.start();
    }

    private void stopMonitor() {
        try {
            jMonitorEntity.junmonitor();
        } finally {
            pool.close(jMonitorEntity.getId());
            jMonitorEntity = null;
        }
    }

    @Override
    public void destroy() {
        if (pool != null) {
            stopMonitor();
            jediSQL = null;
        }
        System.gc();
    }
}
