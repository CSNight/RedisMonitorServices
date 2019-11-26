package com.csnight.redis.monitor.websocket;

import com.csnight.redis.monitor.utils.YamlConfigUtil;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

import static com.csnight.redis.monitor.utils.BaseUtils.getResourceDir;

public class WebSocketInitializer extends ChannelInitializer<SocketChannel> {
    private ChannelGroup channels;

    WebSocketInitializer(ChannelGroup channelGroup) {
        channels = channelGroup;
    }

    @Override
    public void initChannel(final SocketChannel ch) throws Exception {
        SSLContext sslContext = CreateContext();
        SSLEngine engine = sslContext.createSSLEngine();
        engine.setUseClientMode(false);
        final ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new SslHandler(engine));
        pipeline.addLast("http-request-decoder", new HttpRequestDecoder());
        pipeline.addLast("aggregator", new HttpObjectAggregator(67108864));
        pipeline.addLast("http-response-encoder", new HttpResponseEncoder());
        pipeline.addLast("request-handler", new WebSocketServerProtocolHandler("/websocket_stream"));
        pipeline.addLast("handler", new WebSocketServerHandler(channels));
    }

    private SSLContext CreateContext() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        InputStream stream = new FileInputStream(getResourceDir() + "www.csnight.xyz.pfx");
        keyStore.load(stream, YamlConfigUtil.getStrYmlVal("server.ssl.key-store-password").toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());//getDefaultAlgorithm:获取默认的 KeyManagerFactory 算法名称。
        kmf.init(keyStore, YamlConfigUtil.getStrYmlVal("server.ssl.key-store-password").toCharArray());
        //SSLContext的实例表示安全套接字协议的实现，它充当用于安全套接字工厂或 SSLEngine 的工厂。
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);
        return sslContext;
    }
}
