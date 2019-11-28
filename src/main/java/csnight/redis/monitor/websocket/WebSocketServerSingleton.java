package csnight.redis.monitor.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.ImmediateEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketServerSingleton {
    private static WebSocketServerSingleton ourInstance;
    private static final Logger logger = LoggerFactory.getLogger(WebSocketServerSingleton.class);
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private final ChannelGroup channelGroup = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);
    private Channel chs;
    private String host;
    private int port;

    public static WebSocketServerSingleton getInstance() {
        if (ourInstance == null) {
            synchronized (WebSocketServerSingleton.class) {
                if (ourInstance == null) {
                    ourInstance = new WebSocketServerSingleton();
                }
            }
        }
        return ourInstance;
    }

    private WebSocketServerSingleton() {
    }

    public ChannelGroup getChannelGroup() {
        return channelGroup;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }


    public void run() throws Exception {
        try {
            final ServerBootstrap b = new ServerBootstrap();
            b.group(workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new WebSocketInitializer(channelGroup));
            chs = b.bind(host, port).sync().channel();
            logger.info("WebSocketServer has started");
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }

    public void sendAll(String text) {
        for (Channel ch : channelGroup) {
            if (ch != null && ch.isOpen()) {
                ch.writeAndFlush(new TextWebSocketFrame(text));
            }
        }
    }

    public void send(String text, ChannelId id) {
        for (Channel ch : channelGroup) {
            if (ch != null && ch.isOpen() && ch.id().equals(id)) {
                ch.writeAndFlush(new TextWebSocketFrame(text));
            }
        }
    }

    public void shutdown() {
        try {
            channelGroup.writeAndFlush(new CloseWebSocketFrame());
            chs.closeFuture();
            channelGroup.close();
            logger.info("WebSocketServer has stopped");
            workerGroup.shutdownGracefully().await();
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }
}
