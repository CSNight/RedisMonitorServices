package csnight.redis.monitor.msg;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.ImmediateEventExecutor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MsgBus {
    private Map<String, Set<String>> UserChannels = new ConcurrentHashMap<>();
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

    public Map<String, Set<String>> getUserChannels() {
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
        Set<String> channels = UserChannels.get(user_id);
        if (channels != null) {
            channels.add(ch.getId());
        } else {
            Set<String> chs = new HashSet<>();
            chs.add(ch.getId());
            UserChannels.put(user_id, chs);
        }
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
            che.getChannel().close();
            channelGroup.remove(che.getChannel());
            channels.remove(cid);
        }
        String rem = "";
        for (String key : UserChannels.keySet()) {
            if (UserChannels.get(key).contains(cid)) {
                UserChannels.get(key).remove(cid);
                rem = key;
                break;
            }
        }
        if (!rem.equals("") && UserChannels.get(rem).size() == 0) {
            UserChannels.remove(rem);
        }
    }

    public void removeAll() {
        channels.values().forEach(che -> {
            che.getChannel().close();
            channelGroup.remove(che.getChannel());
        });
        channels.clear();
        UserChannels.clear();
    }

    public void dispatchMsg(JSONObject msg) {
        int msgType = msg.getInteger("rt");
        switch (CmdMsgType.getEnumType(msgType)) {
            default:
            case UNKNOWN:
                break;
            case CONNECT:
                break;
            case DISCONNECT:
                break;
            case CMD:
                break;
            case PUB:
                break;
            case SUB:
                break;
            case DESUB:
                break;
        }

    }
}
