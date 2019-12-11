package csnight.redis.monitor.websocket;

import csnight.redis.monitor.msg.MsgBus;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketServerHandler.class);
    private ChannelGroup channels;

    WebSocketServerHandler(ChannelGroup channelGroup) {
        channels = channelGroup;
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, DefaultFullHttpResponse res) {
        // 返回应答给客户端
        if (res.status().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
        }
        // 如果是非Keep-Alive，关闭连接
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        //noinspection deprecation
        if (!HttpHeaders.isKeepAlive(req) || res.status().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object Frame) {
        if (Frame instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) Frame);
        } else if (Frame instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) Frame);
        }
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        System.out.println("Remote Client disconnected!");
        MsgBus.getIns().remove(ctx.channel().id().toString());
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            WebSocketServerProtocolHandler.HandshakeComplete complete = (WebSocketServerProtocolHandler.HandshakeComplete) evt;
            String uri = complete.requestUri();
            ctx.channel().writeAndFlush(new TextWebSocketFrame(ctx.channel().id().toString()));
            channels.add(ctx.channel());
        }
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        // 如果HTTP解码失败，返回HTTP异常
        if (!req.decoderResult().isSuccess() || (!"websocket".equals(req.headers().get("Upgrade")))) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }//获取url后置参数
        String uri = req.uri();
        // Handshake
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req, uri), null, false);
        WebSocketServerHandshaker hand_shaker = wsFactory.newHandshaker(req);
        if (hand_shaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            ChannelFuture handshakeFuture = hand_shaker.handshake(ctx.channel(), req);
            handshakeFuture.addListener((ChannelFutureListener) future -> {
                if (!future.isSuccess()) {
                    ctx.fireExceptionCaught(future.cause());
                } else {
                    ctx.fireUserEventTriggered(WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE);
                }
            });
        }
    }

    private String getWebSocketLocation(FullHttpRequest req, String uri) {
        return "wss://" + req.headers().get(HttpHeaderNames.HOST) + uri;
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof TextWebSocketFrame) {
            String clientMsg = ((TextWebSocketFrame) frame).text();
            System.out.println(clientMsg);
            System.out.println(ctx.channel().id().toString());
        } else if (frame instanceof BinaryWebSocketFrame) {
            logger.info("Binary msg received");
            ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.isFinalFragment(), frame.rsv(), frame.copy().content()));
        } else if (frame instanceof PingWebSocketFrame) {
            logger.info("Ping msg received");
            ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.isFinalFragment(), frame.rsv(), frame.copy().content()));
        } else if (frame instanceof CloseWebSocketFrame) {
            ctx.channel().close();
            logger.info("Ping msg received");
        } else if (frame instanceof PongWebSocketFrame) {
            logger.info("Pong msg received");
            ctx.channel().writeAndFlush(new PingWebSocketFrame(frame.isFinalFragment(), frame.rsv(), frame.copy().content()));
        } else {
            logger.error(String.format("%s frame types not supported", frame.getClass().getName()));
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        channels.remove(ctx.channel());
        ctx.channel().close();
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
    }

}
