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

    /**
     * 功能描述: 管道注册
     *
     * @param user_id 用户名
     * @param channel channel
     * @author csnight
     * @since 2019/12/27 8:50
     */
    public void register(String user_id, Channel channel) {
        ChannelEntity ch = new ChannelEntity(ChannelType.COMMON, channel, user_id);
        ch.setAuthorities(BaseUtils.GetUserAuthorities(user_id));
        ch.setCommandsAuth(BaseUtils.GetUserCmdAuth(user_id));
        channels.put(ch.getId(), ch);
        channelGroup.add(channel);
        UserChannels.put(channel.id().asShortText(), user_id);
    }

    /**
     * 功能描述: 设置管道类型
     *
     * @param ct  管道类型
     * @param cid 管道id
     * @author csnight
     * @since 2019/12/27 8:50
     */
    public void setChannelType(ChannelType ct, String cid) {
        ChannelEntity entity = channels.get(cid);
        if (entity != null) {
            entity.setCt(ct);
        }
    }

    /**
     * 功能描述: 移除管道
     *
     * @param cid 管道id
     * @author csnight
     * @since 2019/12/27 8:51
     */
    public void remove(String cid) {
        ChannelEntity che = channels.get(cid);
        if (che != null) {
            //销毁管道处理器实例
            if (che.getCt().equals(ChannelType.PUBSUB)) {
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

    /**
     * 功能描述: 清理用户管道
     *
     * @param uid 用户id
     * @author csnight
     * @since 2019/12/27 8:52
     */
    public void ClearUserChannel(String uid) {
        Set<String> channelIdx = new HashSet<>();
        UserChannels.entrySet().stream().filter(item -> item.getValue().equals(uid)).forEach(chs -> channelIdx.add(chs.getKey()));
        channelIdx.forEach(this::remove);
    }

    /**
     * 功能描述: 清理所有管道
     *
     * @author csnight
     * @since 2019/12/27 8:52
     */
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
