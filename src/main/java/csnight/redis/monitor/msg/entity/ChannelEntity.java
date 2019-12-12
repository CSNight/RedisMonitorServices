package csnight.redis.monitor.msg.entity;

import csnight.redis.monitor.msg.handler.WsChannelHandler;
import csnight.redis.monitor.msg.series.ChannelType;
import io.netty.channel.Channel;

import java.util.List;

public class ChannelEntity {
    private ChannelType ct;
    private Channel channel;
    private String id;
    private String user_id;
    private List<WsChannelHandler> handlers;

    public ChannelEntity(ChannelType ct, Channel ch, String user_id) {
        this.ct = ct;
        this.channel = ch;
        this.user_id = user_id;
        this.id = ch.id().toString();
    }

    public ChannelType getCt() {
        return ct;
    }

    public void setCt(ChannelType ct) {
        this.ct = ct;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public List<WsChannelHandler> getHandlers() {
        return handlers;
    }
}
