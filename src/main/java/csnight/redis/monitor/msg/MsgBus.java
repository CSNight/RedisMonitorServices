package csnight.redis.monitor.msg;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.msg.entity.ChannelEntity;
import csnight.redis.monitor.msg.entity.WssResponseEntity;
import csnight.redis.monitor.msg.series.ChannelType;
import csnight.redis.monitor.msg.series.ResponseMsgType;
import csnight.redis.monitor.utils.BaseUtils;
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
        ConsoleMsgDispatcher cmd = ConsoleMsgDispatcher.getIns();
        cmd.setChannels(channels);
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
        ch.setAuthorities(BaseUtils.GetUserAuthorities(user_id));
        ch.setCommandsAuth(BaseUtils.GetUserCmdAuth(user_id));
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
        WssResponseEntity wre;
        String appId = msg.getString("appId");
        String msgType = msg.getString("ct");
        try {
            switch (msgType) {
                case "console":
                    if (channels.get(ch.id().asShortText()).hasAuthorize("INS_CONSOLE")) {
                        ConsoleMsgDispatcher.getIns().dispatchMsg(msg, ch);
                    }
                    break;
                case "dt_operation":
                    if (channels.get(ch.id().asShortText()).hasAuthorize("INS_DT_OP")) {

                    }
                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
            wre = new WssResponseEntity(ResponseMsgType.ERROR, ex.getMessage());
            wre.setAppId(appId);
            WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), ch);
        }
    }


}
