package csnight.redis.monitor.msg.entity;

import csnight.redis.monitor.exception.ValidateException;
import csnight.redis.monitor.msg.handler.WsChannelHandler;
import csnight.redis.monitor.msg.series.ChannelType;
import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelEntity {
    private ChannelType ct;
    private Channel channel;
    private String id;
    private String user_id;
    private Map<String, Set<String>> commandsAuth = new HashMap<>();
    private Set<String> authorities = new HashSet<>();
    private Map<String, WsChannelHandler> handlers = new ConcurrentHashMap<>();

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

    public Set<String> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<String> authorities) {
        this.authorities = authorities;
    }

    public void setCommandsAuth(Map<String, Set<String>> commandsAuth) {
        this.commandsAuth = commandsAuth;
    }

    public Map<String, WsChannelHandler> getHandlers() {
        return handlers;
    }

    public boolean hasAuthorize(String authorize) {
        if (authorities.contains(authorize)) {
            return true;
        } else {
            throw new ValidateException("Dose not has authorize");
        }
    }

    public boolean canExecute(String ins, String authorize) {
        Set<String> commands = commandsAuth.get(ins);
        if (commands != null && commands.contains(authorize)) {
            return true;
        } else {
            throw new ValidateException("Dose not has authorize to execute command: " + authorize);
        }
    }
}
