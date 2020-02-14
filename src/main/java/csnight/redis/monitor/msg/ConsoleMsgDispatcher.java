package csnight.redis.monitor.msg;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.exception.CmdMsgException;
import csnight.redis.monitor.msg.entity.ChannelEntity;
import csnight.redis.monitor.msg.entity.WssResponseEntity;
import csnight.redis.monitor.msg.handler.CmdRespHandler;
import csnight.redis.monitor.msg.handler.MonitorHandler;
import csnight.redis.monitor.msg.handler.SubscribeHandler;
import csnight.redis.monitor.msg.handler.WsChannelHandler;
import csnight.redis.monitor.msg.series.ChannelType;
import csnight.redis.monitor.msg.series.CmdMsgType;
import csnight.redis.monitor.msg.series.ResponseMsgType;
import csnight.redis.monitor.websocket.WebSocketServer;
import io.netty.channel.Channel;

import java.util.Map;

public class ConsoleMsgDispatcher {
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
        WssResponseEntity wre;
        int requestType = msg.getIntValue("rt");
        String appId = msg.getString("appId");
        if (!checkAuthority(msg, requestType, ch)) {
            wre = new WssResponseEntity(ResponseMsgType.ERROR, "Does not has authority to execute");
            wre.setAppId(appId);
            WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), ch);
        }
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
                    Map<String, WsChannelHandler> handlers = channels.get(ch.id().asShortText()).getHandlers();
                    for (Map.Entry<String, WsChannelHandler> entry : handlers.entrySet()) {
                        if (entry.getKey().equals(appId)) {
                            entry.getValue().destroy();
                            entry.setValue(null);
                        }
                    }
                    MsgBus.getIns().setChannelType(ChannelType.COMMON, ch.id().asShortText());
                    handlers.remove(appId);
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
                    Map<String, WsChannelHandler> mHandlers = channels.get(ch.id().asShortText()).getHandlers();
                    for (Map.Entry<String, WsChannelHandler> entry : mHandlers.entrySet()) {
                        if (entry.getKey().equals(appId)) {
                            entry.getValue().destroy();
                        }
                    }
                    MsgBus.getIns().setChannelType(ChannelType.COMMON, ch.id().asShortText());
                    mHandlers.remove(appId);
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

    private boolean checkAuthority(JSONObject msg, int requestType, Channel ch) {
        boolean isAuth;
        ChannelEntity che = channels.get(ch.id().asShortText());
        if (requestType == 100 || requestType == 200 || requestType == 201 || requestType == 204) {
            String command = msg.getString("msg");
            if (command == null || command.split(" ").length == 0) {
                isAuth = false;
            } else {
                isAuth = che.canExecute(command.trim().split(" ")[0].toUpperCase());
            }
        } else if (requestType == 202) {
            isAuth = che.canExecute("UNSUBSCRIBE");
        } else if (requestType == 203) {
            isAuth = che.canExecute("PUNSUBSCRIBE");
        } else isAuth = requestType == 205;
        return isAuth;
    }
}

