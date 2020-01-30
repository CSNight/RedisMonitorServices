package csnight.redis.monitor.msg;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.exception.CmdMsgException;
import csnight.redis.monitor.msg.entity.ChannelEntity;
import csnight.redis.monitor.msg.entity.WssResponseEntity;
import csnight.redis.monitor.msg.series.ResponseMsgType;
import csnight.redis.monitor.msg.series.StatMsgType;
import csnight.redis.monitor.quartz.JobFactory;
import csnight.redis.monitor.utils.IdentifyUtils;
import csnight.redis.monitor.utils.ReflectUtils;
import csnight.redis.monitor.websocket.WebSocketServer;
import io.netty.channel.Channel;

import java.util.Map;

public class StatisticMsgDispatcher {
    private Map<String, ChannelEntity> channels;
    private static StatisticMsgDispatcher ourInstance;

    public static StatisticMsgDispatcher getIns() {
        if (ourInstance == null) {
            synchronized (MsgBus.class) {
                if (ourInstance == null) {
                    ourInstance = new StatisticMsgDispatcher();
                }
            }
        }
        return ourInstance;
    }

    private StatisticMsgDispatcher() {
    }

    public void setChannels(Map<String, ChannelEntity> channels) {
        this.channels = channels;
    }

    public void dispatchMsg(JSONObject msg, Channel ch) throws CmdMsgException {
        WssResponseEntity wre;
        int requestType = msg.getIntValue("rt");
        String appId = msg.getString("appId");
        switch (StatMsgType.getEnumType(requestType)) {
            default:
            case UNKNOWN:
                wre = new WssResponseEntity(ResponseMsgType.UNKNOWN, "Unknown msg type");
                wre.setAppId(appId);
                WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), ch);
                break;
        }
    }

    private boolean clearJob(JSONObject msg, String jobGroup) {
        String ins = msg.getString("ins");
        if (ins == null || ins.equals("")) {
            return false;
        }
        String JobId = IdentifyUtils.string2MD5(ins, "Phs$");
        JobFactory jobFactory = ReflectUtils.getBean(JobFactory.class);
        String res = jobFactory.DeleteJob(JobId, jobGroup);
        return res.equals("success");
    }
}
