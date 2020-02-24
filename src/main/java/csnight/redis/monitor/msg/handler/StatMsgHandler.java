package csnight.redis.monitor.msg.handler;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.busi.task.StatTaskManagerImpl;
import csnight.redis.monitor.msg.entity.WssResponseEntity;
import csnight.redis.monitor.msg.series.ResponseMsgType;
import csnight.redis.monitor.utils.ReflectUtils;
import csnight.redis.monitor.websocket.WebSocketServer;
import io.netty.channel.Channel;

public class StatMsgHandler implements WsChannelHandler {
    private String appId;
    private Channel channel;
    private String ins;
    private StatTaskManagerImpl statTaskManager = ReflectUtils.getBean(StatTaskManagerImpl.class);

    public StatMsgHandler(String appId, Channel ch, String ins) {
        this.appId = appId;
        this.channel = ch;
        this.ins = ins;
    }

    @Override
    public String getIns() {
        return ins;
    }

    @Override
    public void initialize(JSONObject msg) {
        String result = statTaskManager.ModifyRedisStatJobData(ins, channel.id().asShortText(), appId);
        WssResponseEntity wre = new WssResponseEntity(result.equals("success") ? ResponseMsgType.STAT_STARTED : ResponseMsgType.ERROR, result);
        wre.setAppId(appId);
        WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), channel);
    }

    @Override
    public void destroy() {
        String st = statTaskManager.ModifyRedisStatJobData(ins, "", "");
        WssResponseEntity wre = new WssResponseEntity(st.equals("success") ? ResponseMsgType.STAT_STOPPED : ResponseMsgType.ERROR, st);
        wre.setAppId(appId);
        WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), channel);
    }
}
