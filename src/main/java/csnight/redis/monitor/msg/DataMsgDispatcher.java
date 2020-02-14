package csnight.redis.monitor.msg;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.exception.CmdMsgException;
import csnight.redis.monitor.msg.entity.ChannelEntity;
import csnight.redis.monitor.msg.entity.WssResponseEntity;
import csnight.redis.monitor.msg.handler.DtMoveHandler;
import csnight.redis.monitor.msg.handler.KeyWatchHandler;
import csnight.redis.monitor.msg.handler.WsChannelHandler;
import csnight.redis.monitor.msg.series.ChannelType;
import csnight.redis.monitor.msg.series.DataMsgType;
import csnight.redis.monitor.msg.series.ResponseMsgType;
import csnight.redis.monitor.websocket.WebSocketServer;
import io.netty.channel.Channel;

import java.util.Map;

public class DataMsgDispatcher {
    private Map<String, ChannelEntity> channels;
    private static DataMsgDispatcher ourInstance;

    public static DataMsgDispatcher getIns() {
        if (ourInstance == null) {
            synchronized (MsgBus.class) {
                if (ourInstance == null) {
                    ourInstance = new DataMsgDispatcher();
                }
            }
        }
        return ourInstance;
    }

    private DataMsgDispatcher() {
    }

    public void setChannels(Map<String, ChannelEntity> channels) {
        this.channels = channels;
    }

    public void dispatchMsg(JSONObject msg, Channel ch) throws CmdMsgException {
        WssResponseEntity wre;
        int requestType = msg.getIntValue("rt");
        String appId = msg.getString("appId");
        switch (DataMsgType.getEnumType(requestType)) {
            default:
            case UNKNOWN:
                wre = new WssResponseEntity(ResponseMsgType.UNKNOWN, "Unknown msg type");
                wre.setAppId(appId);
                WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), ch);
                break;
            case KEYWATCH:
                KeyWatchHandler keyWatchHandler = new KeyWatchHandler(appId, ch);
                keyWatchHandler.initialize(msg);
                channels.get(ch.id().asShortText()).getHandlers().put(appId, keyWatchHandler);
                MsgBus.getIns().setChannelType(ChannelType.MONITOR, ch.id().asShortText());
                wre = new WssResponseEntity(ResponseMsgType.KEYWATCHCON, "Monitor process start");
                wre.setAppId(appId);
                WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), ch);
                break;
            case UNKEYWATCH:
                channels.get(ch.id().asShortText()).getHandlers().forEach((id, handler) -> {
                    if (id.equals(appId)) {
                        handler.destroy();
                    }
                });
                MsgBus.getIns().setChannelType(ChannelType.COMMON, ch.id().asShortText());
                channels.get(ch.id().asShortText()).getHandlers().remove(appId);
                wre = new WssResponseEntity(ResponseMsgType.DEKEYWATCH, "Unmonitor success");
                wre.setAppId(appId);
                WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), ch);
                break;
            case SHAKESTART:
                DtMoveHandler dtMoveHandler = new DtMoveHandler(appId, ch);
                dtMoveHandler.initialize(msg);
                channels.get(ch.id().asShortText()).getHandlers().put(appId, dtMoveHandler);
                MsgBus.getIns().setChannelType(ChannelType.MONITOR, ch.id().asShortText());
                wre = new WssResponseEntity(ResponseMsgType.SHAKESTART, "Data operation started");
                wre.setAppId(appId);
                WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), ch);
                break;
            case SHAKEEND:
                Map<String, WsChannelHandler> handlers = channels.get(ch.id().asShortText()).getHandlers();
                for (Map.Entry<String, WsChannelHandler> entry : handlers.entrySet()) {
                    if (entry.getKey().equals(appId)) {
                        entry.getValue().destroy();
                        entry.setValue(null);
                    }
                }
                MsgBus.getIns().setChannelType(ChannelType.COMMON, ch.id().asShortText());
                handlers.remove(appId);
                wre = new WssResponseEntity(ResponseMsgType.SHAKEFINISH, "Data operation finished");
                wre.setAppId(appId);
                WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), ch);
                break;
        }
    }
}
