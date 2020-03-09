package csnight.redis.monitor.websocket;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.db.repos.SysUserRepository;
import csnight.redis.monitor.msg.MsgBus;
import csnight.redis.monitor.msg.entity.WssResponseEntity;
import csnight.redis.monitor.msg.series.ResponseMsgType;
import csnight.redis.monitor.utils.ReflectUtils;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketServerHandler.class);
    private ChannelGroup channels;

    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, DefaultFullHttpResponse res) {
        HttpResponseStatus responseStatus = res.status();
        if (responseStatus.code() != 200) {
            ByteBufUtil.writeUtf8(res.content(), responseStatus.toString());
            HttpUtil.setContentLength(res, res.content().readableBytes());
        }
        // Send the response and close the connection if necessary.
        boolean keepAlive = HttpUtil.isKeepAlive(req) && responseStatus.code() == 200;
        HttpUtil.setKeepAlive(res, keepAlive);
        ChannelFuture future = ctx.write(res); // Flushed in channelReadComplete()
        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
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
            Map<String, String> params = new HashMap<>();
            QueryStringDecoder decoder = new QueryStringDecoder(complete.requestUri());
            if (decoder.parameters() != null) {
                decoder.parameters().forEach((key, value) -> {
                    params.put(key, value.get(0));
                });
                String uid = params.get("uid");
                if (uid != null && UidCheck(uid)) {
                    MsgBus.getIns().register(params.get("uid"), ctx.channel());
                    WssResponseEntity entity = new WssResponseEntity(ResponseMsgType.INIT, ctx.channel().id().asShortText());
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(entity)));
                } else {
                    WssResponseEntity entity = new WssResponseEntity(ResponseMsgType.ERROR, "Please connect with correct uid");
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(entity)));
                    ctx.channel().close();
                    ctx.fireChannelInactive();
                }
            } else {
                ctx.channel().close();
                ctx.fireChannelInactive();
            }
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
            hand_shaker.handshake(ctx.channel(), req);
        }
    }

    private String getWebSocketLocation(FullHttpRequest req, String uri) {
        return "wss://" + req.headers().get(HttpHeaderNames.HOST) + uri;
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof TextWebSocketFrame) {
            String clientMsg = ((TextWebSocketFrame) frame).text();
            try {
                JSONObject msg = JSONObject.parseObject(clientMsg);
                MsgBus.getIns().dispatchMsg(msg, ctx.channel());
            } catch (Exception ex) {
                WssResponseEntity err = new WssResponseEntity(ResponseMsgType.ERROR, "Msg parse error");
                WebSocketServer.getInstance().send(JSONObject.toJSONString(err), ctx.channel());
            }
        } else if (frame instanceof BinaryWebSocketFrame) {
            logger.info("Binary msg received");
            ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.isFinalFragment(), frame.rsv(), frame.copy().content()));
        } else if (frame instanceof PingWebSocketFrame) {
            logger.info("Ping msg received");
            ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.isFinalFragment(), frame.rsv(), frame.copy().content()));
        } else if (frame instanceof CloseWebSocketFrame) {
            ctx.channel().close();
            logger.info("Close msg received");
        } else if (frame instanceof PongWebSocketFrame) {
            logger.info("Pong msg received");
            ctx.channel().writeAndFlush(new PingWebSocketFrame(frame.isFinalFragment(), frame.rsv(), frame.copy().content()));
        } else {
            logger.error(String.format("%s frame types not supported", frame.getClass().getName()));
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        logger.error(cause.getMessage());
    }

    private boolean UidCheck(String uid) {
        SysUserRepository userRepository = ReflectUtils.getBean(SysUserRepository.class);
        return userRepository.existsById(uid);
    }

}
