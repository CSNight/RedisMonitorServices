package csnight.redis.monitor.quartz.jobs;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.msg.MsgBus;
import csnight.redis.monitor.msg.entity.ChannelEntity;
import csnight.redis.monitor.msg.entity.WssResponseEntity;
import csnight.redis.monitor.msg.series.ResponseMsgType;
import csnight.redis.monitor.redis.pool.MultiRedisPool;
import csnight.redis.monitor.redis.pool.RedisPoolInstance;
import csnight.redis.monitor.redis.statistic.InfoCmdParser;
import csnight.redis.monitor.websocket.WebSocketServer;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Map;

public class Job_StatisticCollect implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        Map<String, String> params = (Map<String, String>) jobDataMap.get("params");
        RedisPoolInstance pool = MultiRedisPool.getInstance().getPool(params.get("ins_id"));
        if (pool == null) {
            return;
        }
        Map<String, Map<String, String>> infos = InfoCmdParser.GetInfoAll(pool);
        System.out.println(JSONObject.toJSONString(infos));
        ChannelEntity che = MsgBus.getIns().getChannels().get(params.get("cid"));
        if (che == null || params.get("appId").equals("")) {
            return;
        }
        WssResponseEntity wre = new WssResponseEntity(ResponseMsgType.RMS_STAT, infos);
        wre.setAppId(params.get("appId"));
        WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), che.getChannel());
    }
}
