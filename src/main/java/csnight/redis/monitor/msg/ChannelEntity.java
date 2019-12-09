package csnight.redis.monitor.msg;

import io.netty.channel.Channel;

public class ChannelEntity {
    private ChannelType ct;
    private Channel channel;
    private String id;
    private String user_id;

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
}
