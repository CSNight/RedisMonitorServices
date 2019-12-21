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
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.ImmediateEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MsgBus {
    private Logger _log = LoggerFactory.getLogger(MsgBus.class);
    private Map<String, String> UserChannels = new ConcurrentHashMap<>();
    private Map<String, ChannelEntity> channels = new ConcurrentHashMap<>();
    private final ChannelGroup channelGroup = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);
    private static MsgBus ourInstance;

    public static MsgBus getIns() {
        if (ourInstance == null) {
            synchronized (MsgBus.class) {
                if (ourInstance == null) {
                    ourInstance = new MsgBus();
                }
            }
        }
        return ourInstance;
    }

    private MsgBus() {
    }

    public Map<String, String> getUserChannels() {
        return UserChannels;
    }

    public Map<String, ChannelEntity> getChannels() {
        return channels;
    }

    public ChannelGroup getChannelGroup() {
        return channelGroup;
    }

    public void register(String user_id, Channel channel) {
        ChannelEntity ch = new ChannelEntity(ChannelType.COMMON, channel, user_id);
        channels.put(ch.getId(), ch);
        channelGroup.add(channel);
        UserChannels.put(channel.id().asShortText(), user_id);
    }

    public void setChannelType(ChannelType ct, String cid) {
        ChannelEntity entity = channels.get(cid);
        if (entity != null) {
            entity.setCt(ct);
        }
    }

    public void remove(String cid) {
        ChannelEntity che = channels.get(cid);
        if (che != null) {
            if (che.getCt().equals(ChannelType.PUBSUB)) {
                //TODO unsubscribe
                che.getHandlers().forEach((ent, handler) -> {
                    handler.destroy();
                });
                che.getHandlers().clear();
            }
            che.getChannel().close();
            channelGroup.remove(che.getChannel());
            channels.remove(cid);
            UserChannels.remove(cid);
        }
    }

    public void ClearUserChannel(String uid) {
        Set<String> channelIdx = new HashSet<>();
        UserChannels.entrySet().stream().filter(item -> item.getValue().equals(uid)).forEach(chs -> channelIdx.add(chs.getKey()));
        channelIdx.forEach(this::remove);
    }

    public void removeAll() {
        channels.values().forEach(che -> {
            che.getChannel().close();
            che.getHandlers().forEach((ent, handler) -> {
                handler.destroy();
            });
            che.getHandlers().clear();
            channelGroup.remove(che.getChannel());
        });
        channels.clear();
        UserChannels.clear();
    }

    public void dispatchMsg(JSONObject msg, Channel ch) {
        _log.info(msg.toJSONString());
        WssResponseEntity wre = null;
        int msgType = msg.getIntValue("rt");
        String appId = msg.getString("appId");
        try {
            switch (CmdMsgType.getEnumType(msgType)) {
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
                    SubscribeHandler subscribeHandler = new SubscribeHandler(appId, ch, CmdMsgType.getEnumType(msgType));
                    subscribeHandler.initialize(msg);
                    channels.get(ch.id().asShortText()).getHandlers().put(appId, subscribeHandler);
                    setChannelType(ChannelType.PUBSUB, ch.id().asShortText());
                    subscribeHandler.startSubscribe();
                    break;
                case DESUB:
                case DEPSUB:
                    channels.get(ch.id().asShortText()).getHandlers().forEach((id, handler) -> {
                        if (id.equals(appId)) {
                            handler.destroy();
                        }
                    });
                    setChannelType(ChannelType.COMMON, ch.id().asShortText());
                    channels.get(ch.id().asShortText()).getHandlers().remove(appId);
                    wre = new WssResponseEntity(ResponseMsgType.DESUB, "Unsubscribe success");
                    wre.setAppId(appId);
                    WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), ch);
                    break;
                case MONITOR:
                    MonitorHandler monitorHandler = new MonitorHandler(appId, ch);
                    monitorHandler.initialize(msg);
                    channels.get(ch.id().asShortText()).getHandlers().put(appId, monitorHandler);
                    setChannelType(ChannelType.MONITOR, ch.id().asShortText());
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
                    setChannelType(ChannelType.COMMON, ch.id().asShortText());
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
