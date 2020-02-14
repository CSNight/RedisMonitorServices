package csnight.redis.monitor.msg;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.msg.entity.ChannelEntity;
import csnight.redis.monitor.msg.entity.WssResponseEntity;
import csnight.redis.monitor.msg.handler.StatMsgHandler;
import csnight.redis.monitor.msg.handler.WsChannelHandler;
import csnight.redis.monitor.msg.series.ChannelType;
import csnight.redis.monitor.msg.series.ResponseMsgType;
import csnight.redis.monitor.msg.series.StatMsgType;
import csnight.redis.monitor.websocket.WebSocketServer;
import io.netty.channel.Channel;

import java.util.Map;

public class StatisticMsgDispatcher {
    private Map<String, ChannelEntity> channels;
    private static StatisticMsgDispatcher ourInstance;


    public static StatisticMsgDispatcher getIns() {
        if (ourInstance == null) {
            synchronized (MsgBus.class) {
                if (ourInstance == null) {
                    ourInstance = new StatisticMsgDispatcher();
                }
            }
        }
        return ourInstance;
    }

    private StatisticMsgDispatcher() {
    }

    public void setChannels(Map<String, ChannelEntity> channels) {
        this.channels = channels;
    }

    public void dispatchMsg(JSONObject msg, Channel ch) {
        WssResponseEntity wre;
        int requestType = msg.getIntValue("rt");
        String appId = msg.getString("appId");
        switch (StatMsgType.getEnumType(requestType)) {
            default:
            case UNKNOWN:
                wre = new WssResponseEntity(ResponseMsgType.UNKNOWN, "Unknown msg type");
                wre.setAppId(appId);
                WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), ch);
                break;
            case STAT_START:
                StatMsgHandler statMsgHandler = new StatMsgHandler(appId, ch, msg.getString("ins"));
                statMsgHandler.initialize(msg);
                channels.get(ch.id().asShortText()).getHandlers().put(appId, statMsgHandler);
                MsgBus.getIns().setChannelType(ChannelType.MONITOR, ch.id().asShortText());
                break;
            case STAT_STOP:
                Map<String, WsChannelHandler> handlers = channels.get(ch.id().asShortText()).getHandlers();
                for (Map.Entry<String, WsChannelHandler> entry : handlers.entrySet()) {
                    if (entry.getKey().equals(appId)) {
                        entry.getValue().destroy();
                        entry.setValue(null);
                    }
                }
                handlers.remove(appId);
                MsgBus.getIns().setChannelType(ChannelType.COMMON, ch.id().asShortText());
                break;
        }
    }
}
