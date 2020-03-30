package csnight.redis.monitor.websocket;

import csnight.redis.monitor.utils.ReflectUtils;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

public class WebSocketInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    public void initChannel(final SocketChannel ch) throws Exception {
        SSLContext sslContext = CreateContext();
        SSLEngine engine = sslContext.createSSLEngine();
        engine.setUseClientMode(false);
        final ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("ssl", new SslHandler(engine));
        pipeline.addLast("http-request-decoder", new HttpRequestDecoder());
        pipeline.addLast("aggregator", new HttpObjectAggregator(67108864));
        pipeline.addLast("http-response-encoder", new HttpResponseEncoder());
        pipeline.addLast("request-handler", new WebSocketServerProtocolHandler("/websocket_stream", true));
        pipeline.addLast("handler", new WebSocketServerHandler());
    }

    private SSLContext CreateContext() throws Exception {
        KeyManagerFactory kmf = ReflectUtils.getBean(KeyManagerFactory.class);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);
        return sslContext;
    }
}
