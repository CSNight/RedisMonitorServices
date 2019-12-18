package csnight.redis.monitor.msg.entity;

import com.alibaba.fastjson.JSONObject;
import com.csnight.jedisql.JediSQL;
import com.csnight.jedisql.JedisPubSub;
import csnight.redis.monitor.msg.series.ResponseMsgType;
import csnight.redis.monitor.utils.IdentifyUtils;
import csnight.redis.monitor.websocket.WebSocketServer;
import io.netty.channel.Channel;


public class PubSubEntity extends JedisPubSub {
    private Channel ch;
    private String id = IdentifyUtils.getUUID();
    private JediSQL jediSQL;
    private String appId;

    public void setCh(Channel ch) {
        this.ch = ch;
    }

    public String getId() {
        return id;
    }

    public JediSQL getJediSQL() {
        return jediSQL;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setJediSQL(JediSQL jediSQL) {
        this.jediSQL = jediSQL;
    }

    // 取得订阅的消息后的处理
    public void onMessage(String channel, String message) {
        WssResponseEntity wre = new WssResponseEntity(ResponseMsgType.PUBSUB, channel + "->" + message);
        wre.setAppId(appId);
        WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), ch);
    }

    // 初始化订阅时候的处理
    public void onSubscribe(String channel, int subscribedChannels) {
        String msg = "Reading messages... (press Ctrl-C to quit)\r\n";
        msg += "1) subscribe on " + channel + "\r\n";
        msg += "2) channel count: " + subscribedChannels + "\r\n";
        msg += "3) timestamp: " + System.currentTimeMillis() + "\r\n";
        WssResponseEntity wre = new WssResponseEntity(ResponseMsgType.SUBCON, msg);
        wre.setAppId(appId);
        WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), ch);
    }

    // 取消订阅时候的处理
    public void onUnsubscribe(String channel, int subscribedChannels) {
        String msg = "1) Cancel subscribe on channel: " + channel + "\r\n";
        msg += "2) subscribe channel count: " + subscribedChannels + "\r\n";
        WssResponseEntity wre = new WssResponseEntity(ResponseMsgType.PUBSUB, msg);
        wre.setAppId(appId);
        WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), ch);
    }

    // 初始化按表达式的方式订阅时候的处理
    public void onPSubscribe(String pattern, int subscribedChannels) {
        String msg = "Reading messages... (press Ctrl-C to quit)\r\n";
        msg += "1) psubscribe on " + pattern + "\r\n";
        msg += "2) channel count: " + subscribedChannels + "\r\n";
        msg += "3) timestamp: " + System.currentTimeMillis() + "\r\n";
        WssResponseEntity wre = new WssResponseEntity(ResponseMsgType.SUBCON, msg);
        wre.setAppId(appId);
        WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), ch);
    }

    // 取消按表达式的方式订阅时候的处理
    public void onPUnsubscribe(String pattern, int subscribedChannels) {
        String msg = "1) Cancel psubscribe on channel: " + pattern + "\r\n";
        msg += "2) psubscribe channel count: " + subscribedChannels + "\r\n";
        WssResponseEntity wre = new WssResponseEntity(ResponseMsgType.PUBSUB, msg);
        wre.setAppId(appId);
        WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), ch);
    }

    // 取得按表达式的方式订阅的消息后的处理
    public void onPMessage(String pattern, String channel, String message) {
        WssResponseEntity wre = new WssResponseEntity(ResponseMsgType.PUBSUB, pattern + ":" + channel + "->" + message);
        wre.setAppId(appId);
        WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), ch);
    }

    public void subscribe(String... channels) {
        jediSQL.subscribe(this, channels);
    }

    public void psubscribe(String... patterns) {
        jediSQL.psubscribe(this, patterns);
    }
}
