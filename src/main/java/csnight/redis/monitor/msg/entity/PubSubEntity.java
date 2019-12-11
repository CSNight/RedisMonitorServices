package csnight.redis.monitor.msg.entity;

import com.alibaba.fastjson.JSONObject;
import com.csnight.jedisql.JedisPubSub;
import csnight.redis.monitor.msg.ResponseMsgType;
import csnight.redis.monitor.websocket.WebSocketServer;
import io.netty.channel.Channel;


public class PubSubEntity extends JedisPubSub {
    private Channel ch;

    public void setCh(Channel ch) {
        this.ch = ch;
    }

    // 取得订阅的消息后的处理
    public void onMessage(String channel, String message) {
        WssResponseEntity wre = new WssResponseEntity(ResponseMsgType.PUBSUB, channel + "->" + message);
        WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), ch);
    }

    // 初始化订阅时候的处理
    public void onSubscribe(String channel, int subscribedChannels) {
        System.out.println(channel + "=" + subscribedChannels);
    }

    // 取消订阅时候的处理
    public void onUnsubscribe(String channel, int subscribedChannels) {
        System.out.println(channel + "=" + subscribedChannels);
    }

    // 初始化按表达式的方式订阅时候的处理
    public void onPSubscribe(String pattern, int subscribedChannels) {
        System.out.println(pattern + "=" + subscribedChannels);
    }

    // 取消按表达式的方式订阅时候的处理
    public void onPUnsubscribe(String pattern, int subscribedChannels) {
        System.out.println(pattern + "=" + subscribedChannels);
    }

    // 取得按表达式的方式订阅的消息后的处理
    public void onPMessage(String pattern, String channel, String message) {
        WssResponseEntity wre = new WssResponseEntity(ResponseMsgType.PUBSUB, pattern + ":" + channel + "->" + message);
        WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), ch);
    }
}
