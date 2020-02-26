package csnight.redis.monitor.msg;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.msg.entity.ChannelEntity;
import csnight.redis.monitor.msg.entity.WssResponseEntity;
import csnight.redis.monitor.msg.handler.ExecMsgHandler;
import csnight.redis.monitor.msg.handler.WsChannelHandler;
import csnight.redis.monitor.msg.series.ChannelType;
import csnight.redis.monitor.msg.series.ExecMsgType;
import csnight.redis.monitor.msg.series.ResponseMsgType;
import csnight.redis.monitor.websocket.WebSocketServer;
import io.netty.channel.Channel;

import java.util.Map;

public class CmdExecMsgDispatcher {
    private Map<String, ChannelEntity> channels;
    private static CmdExecMsgDispatcher ourInstance;

    public static CmdExecMsgDispatcher getIns() {
        if (ourInstance == null) {
            synchronized (MsgBus.class) {
                if (ourInstance == null) {
                    ourInstance = new CmdExecMsgDispatcher();
                }
            }
        }
        return ourInstance;
    }

    private CmdExecMsgDispatcher() {
    }

    public void setChannels(Map<String, ChannelEntity> channels) {
        this.channels = channels;
    }

    public void dispatchMsg(JSONObject msg, Channel ch) {
        WssResponseEntity wre;
        int requestType = msg.getIntValue("rt");
        String appId = msg.getString("appId");
        switch (ExecMsgType.getEnumType(requestType)) {
            default:
            case UNKNOWN:
                wre = new WssResponseEntity(ResponseMsgType.UNKNOWN, "Unknown msg type");
                wre.setAppId(appId);
                WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), ch);
                break;
            case EXEC_START:
                ExecMsgHandler execMsgHandler = new ExecMsgHandler(appId, ch, msg.getString("ins"));
                execMsgHandler.initialize(msg);
                channels.get(ch.id().asShortText()).getHandlers().put(appId, execMsgHandler);
                MsgBus.getIns().setChannelType(ChannelType.MONITOR, ch.id().asShortText());
                break;
            case EXEC_STOP:
                Map<String, WsChannelHandler> handlers = channels.get(ch.id().asShortText()).getHandlers();
                for (Map.Entry<String, WsChannelHandler> entry : handlers.entrySet()) {
                    if (entry.getKey().equals(appId)) {
                        entry.getValue().destroy();
                    }
                }
                handlers.remove(appId);
                MsgBus.getIns().setChannelType(ChannelType.COMMON, ch.id().asShortText());
                break;
        }
    }
}
