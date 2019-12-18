package csnight.redis.monitor.websocket;

import csnight.redis.monitor.msg.MsgBus;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketServer {
    private static WebSocketServer ourInstance;
    private static final Logger logger = LoggerFactory.getLogger(WebSocketServer.class);
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private final ChannelGroup channelGroup = MsgBus.getIns().getChannelGroup();
    private String host;
    private int port;

    public static WebSocketServer getInstance() {
        if (ourInstance == null) {
            synchronized (WebSocketServer.class) {
                if (ourInstance == null) {
                    ourInstance = new WebSocketServer();
                }
            }
        }
        return ourInstance;
    }

    private WebSocketServer() {
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


    public void run() {
        try {
            final ServerBootstrap b = new ServerBootstrap();
            b.group(workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new WebSocketInitializer(channelGroup));
            b.bind(host, port).sync();
            logger.info("WebSocketServer has started");
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }

    public void broadcast(String text) {
        for (Channel ch : channelGroup) {
            if (ch != null && ch.isOpen()) {
                ch.writeAndFlush(new TextWebSocketFrame(text));
            }
        }
        logger.info(text);
    }

    public void send(String text, Channel ch) {
        if (ch != null && ch.isOpen()) {
            ch.writeAndFlush(new TextWebSocketFrame(text));
            logger.info(text);
        }
    }

    public void shutdown() {
        try {
            logger.info("Shutting down WebSocketServer");
            channelGroup.writeAndFlush(new CloseWebSocketFrame());
            channelGroup.close();
            logger.info("WebSocketServer has stopped!");
            workerGroup.shutdownGracefully().await();
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }
}
