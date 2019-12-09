package csnight.redis.monitor.msg;

import io.netty.channel.Channel;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MsgBus {
    private Map<String, Set<String>> UserChannels = new ConcurrentHashMap<>();
    private Map<String, ChannelEntity> channels = new ConcurrentHashMap<>();

    public void register(String user_id, Channel channel) {
        ChannelEntity ch = new ChannelEntity(ChannelType.COMMON, channel, user_id);
        channels.put(ch.getId(), ch);
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
        channels.remove(cid);
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
}
