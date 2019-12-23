package csnight.redis.monitor.msg;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.exception.CmdMsgException;
import csnight.redis.monitor.msg.entity.ChannelEntity;
import csnight.redis.monitor.msg.entity.WssResponseEntity;
import csnight.redis.monitor.msg.handler.CmdRespHandler;
import csnight.redis.monitor.msg.handler.MonitorHandler;
import csnight.redis.monitor.msg.handler.SubscribeHandler;
import csnight.redis.monitor.msg.series.ChannelType;
import csnight.redis.monitor.msg.series.CmdMsgType;
import csnight.redis.monitor.msg.series.ResponseMsgType;
import csnight.redis.monitor.websocket.WebSocketServer;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ConsoleMsgDispatcher {
    private Logger _log = LoggerFactory.getLogger(MsgBus.class);
    private Map<String, ChannelEntity> channels;
    private static ConsoleMsgDispatcher ourInstance;

    public static ConsoleMsgDispatcher getIns() {
        if (ourInstance == null) {
            synchronized (MsgBus.class) {
                if (ourInstance == null) {
                    ourInstance = new ConsoleMsgDispatcher();
                }
            }
        }
        return ourInstance;
    }

    private ConsoleMsgDispatcher() {
    }

    public void setChannels(Map<String, ChannelEntity> channels) {
        this.channels = channels;
    }

    public void dispatchMsg(JSONObject msg, Channel ch) {
        _log.info(msg.toJSONString());
        WssResponseEntity wre;
        int requestType = msg.getIntValue("rt");
        String appId = msg.getString("appId");
        try {
            switch (CmdMsgType.getEnumType(requestType)) {
                default:
                case UNKNOWN:
                    wre = new WssResponseEntity(ResponseMsgType.UNKNOWN, "Unknown msg type");
                    wre.setAppId(appId);
                    WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), ch);
                    break;
                case CMD:
                    CmdRespHandler cmdRespHandler = new CmdRespHandler();
                    cmdRespHandler.initialize(msg);
                    channels.get(ch.id().asShortText()).getHandlers().put(appId, cmdRespHandler);
                    wre = cmdRespHandler.execute();
                    wre.setAppId(appId);
                    WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), ch);
                    cmdRespHandler.destroy();
                    channels.get(ch.id().asShortText()).getHandlers().remove(appId);
                    break;
                case SUB:
                case PSUB:
                    SubscribeHandler subscribeHandler = new SubscribeHandler(appId, ch, CmdMsgType.getEnumType(requestType));
                    subscribeHandler.initialize(msg);
                    channels.get(ch.id().asShortText()).getHandlers().put(appId, subscribeHandler);
                    MsgBus.getIns().setChannelType(ChannelType.PUBSUB, ch.id().asShortText());
                    subscribeHandler.startSubscribe();
                    break;
                case DESUB:
                case DEPSUB:
                    channels.get(ch.id().asShortText()).getHandlers().forEach((id, handler) -> {
                        if (id.equals(appId)) {
                            handler.destroy();
                        }
                    });
                    MsgBus.getIns().setChannelType(ChannelType.COMMON, ch.id().asShortText());
                    channels.get(ch.id().asShortText()).getHandlers().remove(appId);
                    wre = new WssResponseEntity(ResponseMsgType.DESUB, "Unsubscribe success");
                    wre.setAppId(appId);
                    WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), ch);
                    break;
                case MONITOR:
                    MonitorHandler monitorHandler = new MonitorHandler(appId, ch);
                    monitorHandler.initialize(msg);
                    channels.get(ch.id().asShortText()).getHandlers().put(appId, monitorHandler);
                    MsgBus.getIns().setChannelType(ChannelType.MONITOR, ch.id().asShortText());
                    monitorHandler.startMonitor();
                    wre = new WssResponseEntity(ResponseMsgType.MONITORCON, "Monitor process start, press ctrl+c to stop");
                    wre.setAppId(appId);
                    WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), ch);
                    break;
                case DEMONITOR:
                    channels.get(ch.id().asShortText()).getHandlers().forEach((id, handler) -> {
                        if (id.equals(appId)) {
                            handler.destroy();
                        }
                    });
                    MsgBus.getIns().setChannelType(ChannelType.COMMON, ch.id().asShortText());
                    channels.get(ch.id().asShortText()).getHandlers().remove(appId);
                    wre = new WssResponseEntity(ResponseMsgType.DEMONITOR, "Unmonitor success");
                    wre.setAppId(appId);
                    WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), ch);
                    break;
            }
        } catch (CmdMsgException ex) {
            wre = new WssResponseEntity(ResponseMsgType.ERROR, ex.getMessage());
            wre.setAppId(appId);
            WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), ch);
        }
    }
}

