package csnight.redis.monitor.quartz.jobs;

import com.alibaba.fastjson.JSONObject;
import com.csnight.jedisql.JediSQL;
import csnight.redis.monitor.db.jpa.RmsRcsLog;
import csnight.redis.monitor.db.jpa.RmsRosLog;
import csnight.redis.monitor.db.jpa.RmsRpsLog;
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

import java.util.Date;
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
        String cmdInfos = jediSQL.info("commandstats");
        String[] sections = infos.replaceAll(" ", "").split("#");
        String[] cmd_stats = cmdInfos.replace("# Commandstats\r\n", "").split("\r\n");
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
        pool.close(clientId);
        long tm = System.currentTimeMillis();
        RmsRpsLog rpsLog = GetPhysicalStat(tm, params.get("ins_id"), parts);
        RmsRosLog rosLog = GetCommandStat(tm, params.get("ins_id"), parts, cmd_stats);
        RmsRcsLog rcsLog = GetClientStat(tm, params.get("ins_id"), parts);
        System.out.println(JSONObject.toJSONString(rosLog));
        ChannelEntity che = MsgBus.getIns().getChannels().get(params.get("cid"));
        if (che == null || params.get("appId").equals("")) {
            return;
        }
        WssResponseEntity wre = new WssResponseEntity(ResponseMsgType.RMS_STAT, infos);
        wre.setAppId(params.get("appId"));
        WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), che.getChannel());
    }

    private RmsRpsLog GetPhysicalStat(long tm, String ins_id, Map<String, Map<String, String>> sections) {
        RmsRpsLog rpsLog = new RmsRpsLog();
        rpsLog.setTm(new Date(tm));
        rpsLog.setIns_id(ins_id);
        Map<String, String> memory = sections.get("Memory");
        Map<String, String> cpu = sections.get("CPU");
        Map<String, String> stats = sections.get("Stats");
        rpsLog.setMem_us(Long.parseLong(memory.get("used_memory")));
        rpsLog.setMem_rs(Long.parseLong(memory.get("used_memory_rss")));
        rpsLog.setMem_peak(Long.parseLong(memory.get("used_memory_peak")));
        rpsLog.setMem_oh(Long.parseLong(memory.get("used_memory_overhead")));
        rpsLog.setMem_ds(Long.parseLong(memory.get("used_memory_dataset")));
        rpsLog.setMem_fr(Double.parseDouble(memory.get("mem_fragmentation_ratio")));
        rpsLog.setMem_frb(memory.containsKey("mem_fragmentation_bytes") ? Long.parseLong(memory.get("mem_fragmentation_bytes")) : 0L);
        rpsLog.setCpu_su(Double.parseDouble(cpu.get("used_cpu_sys")));
        rpsLog.setCpu_uu(Double.parseDouble(cpu.get("used_cpu_user")));
        rpsLog.setIoi(Long.parseLong(stats.get("total_net_input_bytes")));
        rpsLog.setIoo(Long.parseLong(stats.get("total_net_output_bytes")));
        rpsLog.setIo_iik(Double.parseDouble(stats.get("instantaneous_input_kbps")));
        rpsLog.setIo_iok(Double.parseDouble(stats.get("instantaneous_output_kbps")));
        return rpsLog;
    }

    private RmsRosLog GetCommandStat(long tm, String ins_id, Map<String, Map<String, String>> sections, String[] cmd_stat) {
        RmsRosLog rosLog = new RmsRosLog();
        rosLog.setTm(new Date(tm));
        rosLog.setIns_id(ins_id);
        Map<String, String> stats = sections.get("Stats");
        rosLog.setTcs(Long.parseLong(stats.get("total_commands_processed")));
        rosLog.setOps(Long.parseLong(stats.get("instantaneous_ops_per_sec")));
        rosLog.setPub_ch(Integer.parseInt(stats.get("pubsub_channels")));
        rosLog.setPub_pat(Integer.parseInt(stats.get("pubsub_patterns")));
        JSONObject joCmds = new JSONObject();
        for (String stat : cmd_stat) {
            JSONObject joCmd = new JSONObject();
            String[] cmd = stat.replace("cmdstat_", "").split(":");
            String[] infos = cmd[1].split(",");
            for (String info : infos) {
                String[] part = info.split("=");
                joCmd.put(part[0], part[1]);
            }
            joCmds.put(cmd[0], joCmd);
        }
        rosLog.setCmd_stat(joCmds.toJSONString());
        return rosLog;
    }

    private RmsRcsLog GetClientStat(long tm, String ins_id, Map<String, Map<String, String>> sections) {
        RmsRcsLog rcsLog = new RmsRcsLog();
        rcsLog.setTm(new Date(tm));
        rcsLog.setIns_id(ins_id);
        Map<String, String> stats = sections.get("Stats");
        Map<String, String> clients = sections.get("Clients");
        rcsLog.setTotal_cons_rec(Long.parseLong(stats.get("total_connections_received")));
        rcsLog.setCli_blo(Integer.parseInt(clients.get("blocked_clients")));
        rcsLog.setCli_con(Integer.parseInt(clients.get("connected_clients")));
        rcsLog.setReject_cons(Long.parseLong(stats.get("rejected_connections")));
        return rcsLog;
    }
}
