package csnight.redis.monitor.quartz.jobs;

import com.alibaba.fastjson.JSONObject;
import com.csnight.jedisql.JediSQL;
import csnight.redis.monitor.msg.MsgBus;
import csnight.redis.monitor.msg.entity.ChannelEntity;
import csnight.redis.monitor.msg.entity.WssResponseEntity;
import csnight.redis.monitor.msg.series.ResponseMsgType;
import csnight.redis.monitor.redis.pool.MultiRedisPool;
import csnight.redis.monitor.redis.pool.RedisPoolInstance;
import csnight.redis.monitor.utils.IdentifyUtils;
import csnight.redis.monitor.websocket.WebSocketServer;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.HashMap;
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
        String clientId = IdentifyUtils.getUUID();
        JediSQL jediSQL = pool.getJedis(clientId);
        String infos = jediSQL.info();
        String[] sections = infos.replaceAll(" ", "").split("#");
        Map<String, Map<String, String>> parts = new HashMap<>();
        for (String section : sections) {
            if (section.equals("")) {
                continue;
            }
            String[] sec = section.split("\r\n");
            Map<String, String> items = new HashMap<>();
            for (int j = 1; j < sec.length; j++) {
                if (sec[j].equals("")) {
                    continue;
                }
                String[] kv = sec[j].split(":");
                if (sec[j].contains("executable:") || sec[j].contains("config_file:")) {
                    items.put(kv[0], sec[j].substring(kv[0].length() + 1));
                } else {
                    items.put(kv[0], String.valueOf(kv[1]));
                }
            }
            parts.put(sec[0], items);
        }
        System.out.println(JSONObject.toJSONString(parts));
        pool.close(clientId);
        ChannelEntity che = MsgBus.getIns().getChannels().get(params.get("cid"));
        if (che == null || params.get("appId").equals("")) {
            return;
        }
        WssResponseEntity wre = new WssResponseEntity(ResponseMsgType.RMS_STAT, infos);
        wre.setAppId(params.get("appId"));
        WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), che.getChannel());
    }
}
