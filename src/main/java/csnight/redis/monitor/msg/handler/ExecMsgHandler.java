package csnight.redis.monitor.msg.handler;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.busi.task.CETaskManagerImpl;
import csnight.redis.monitor.msg.entity.WssResponseEntity;
import csnight.redis.monitor.msg.series.ResponseMsgType;
import csnight.redis.monitor.utils.ReflectUtils;
import csnight.redis.monitor.websocket.WebSocketServer;
import io.netty.channel.Channel;

import java.util.HashSet;
import java.util.Set;

public class ExecMsgHandler implements WsChannelHandler {
    private String appId;
    private Channel channel;
    private CETaskManagerImpl ceTaskManager = ReflectUtils.getBean(CETaskManagerImpl.class);
    private Set<String> jobIds = new HashSet<>();
    private String ins;

    public ExecMsgHandler(String appId, Channel ch, String ins) {
        this.appId = appId;
        this.channel = ch;
        this.ins = ins;
    }

    @Override
    public String getIns() {
        return ins;
    }

    public boolean checkJobPipeExist(String jobId) {
        return jobIds.contains(jobId);
    }

    public String getAppId() {
        return appId;
    }

    public String getChannelId() {
        return channel.id().asShortText();
    }

    @Override
    public void initialize(JSONObject msg) {
        WssResponseEntity wre = new WssResponseEntity(ResponseMsgType.EXEC_STARTED, "start");
        wre.setAppId(appId);
        WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), channel);
    }

    public void addJobPipeline(JSONObject msg) {
        String jobId = msg.getString("ins");
        jobIds.add(jobId);
        String result = ceTaskManager.ModifyRedisCeJobData(jobId, channel.id().asShortText(), appId);
        WssResponseEntity wre = new WssResponseEntity(result.equals("success") ? ResponseMsgType.EXEC_ADDED : ResponseMsgType.ERROR, result);
        wre.setAppId(appId);
        WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), channel);
    }

    public void removeJobPipeline(JSONObject msg) {
        String jobId = msg.getString("ins");
        String st = ceTaskManager.ModifyRedisCeJobData(jobId, "", "");
        WssResponseEntity wre = new WssResponseEntity(st.equals("success") ? ResponseMsgType.EXEC_REMOVED : ResponseMsgType.ERROR, st);
        wre.setAppId(appId);
        WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), channel);
    }

    @Override
    public void destroy() {
        for (String jobId : jobIds) {
            ceTaskManager.ModifyRedisCeJobData(jobId, "", "");
        }
        jobIds.clear();
        WssResponseEntity wre = new WssResponseEntity(ResponseMsgType.EXEC_STOPPED, "all pipeline shutdown");
        wre.setAppId(appId);
        WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), channel);
    }
}
