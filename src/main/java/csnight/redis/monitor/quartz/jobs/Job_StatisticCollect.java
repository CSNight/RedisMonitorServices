package csnight.redis.monitor.quartz.jobs;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.csnight.jedisql.JediSQL;
import csnight.redis.monitor.db.jpa.*;
import csnight.redis.monitor.monitor.MonitorBus;
import csnight.redis.monitor.msg.MsgBus;
import csnight.redis.monitor.msg.entity.ChannelEntity;
import csnight.redis.monitor.msg.entity.WssResponseEntity;
import csnight.redis.monitor.msg.series.ResponseMsgType;
import csnight.redis.monitor.redis.pool.MultiRedisPool;
import csnight.redis.monitor.redis.pool.RedisPoolInstance;
import csnight.redis.monitor.redis.statistic.RmsLogAsyncPool;
import csnight.redis.monitor.utils.IdentifyUtils;
import csnight.redis.monitor.utils.ReflectUtils;
import csnight.redis.monitor.websocket.WebSocketServer;
import org.quartz.*;

import java.util.*;

//持久化job上一次的data 计算每秒均值
@PersistJobDataAfterExecution
public class Job_StatisticCollect implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        JobKey jobKey = context.getJobDetail().getKey();
        String rules = MonitorBus.getIns().GetEnableByCache(jobKey.getName());
        System.out.println(rules);
        Map<String, String> params = (Map<String, String>) jobDataMap.get("params");
        RedisPoolInstance pool = MultiRedisPool.getInstance().getPool(params.get("ins_id"));
        if (pool == null) {
            try {
                context.getScheduler().pauseJob(context.getJobDetail().getKey());
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
            return;
        }
        String clientId = IdentifyUtils.getUUID();
        JediSQL jediSQL;
        try {
            jediSQL = pool.getJedis(clientId);
        } catch (Exception ex) {
            return;
        }
        if (jediSQL == null) {
            return;
        }
        String infos = jediSQL.info();
        String cmdInfos = jediSQL.info("commandstats");
        String[] sections = infos.replaceAll(" ", "").split("#");
        String[] cmd_stats = cmdInfos.replace("# Commandstats\r\n", "").split("\r\n");
        String[] clients = jediSQL.clientList().split("\n");
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
        RmsRpsLog rpsLog = GetPhysicalStat(tm, params.get("ins_id"), parts, params);
        RmsRosLog rosLog = GetCommandStat(tm, params.get("ins_id"), parts, cmd_stats);
        RmsRcsLog rcsLog = GetClientStat(tm, params.get("ins_id"), parts, clients);
        RmsRksLog rksLog = GetKeysStat(tm, params.get("ins_id"), parts, params);
        RmsLogAsyncPool rmsLogAsyncPool = ReflectUtils.getBean(RmsLogAsyncPool.class);
        rmsLogAsyncPool.offer(rpsLog);
        rmsLogAsyncPool.offer(rosLog);
        rmsLogAsyncPool.offer(rcsLog);
        rmsLogAsyncPool.offer(rksLog);
        parts.clear();
        parts = null;
        infos = null;
        sections = null;
        clients = null;
        params.put("tm", String.valueOf(tm));
        jobDataMap.put("params", params);
        checkMonitorRule(rules, rpsLog, rcsLog, rksLog, rosLog);
        //存在ID为空的 直接返回
        if (params.get("cid").equals("") || params.get("appId").equals("")) {
            return;
        }
        //channelId不为空 则查询通道实体
        ChannelEntity che = MsgBus.getIns().getChannels().get(params.get("cid"));
        if (che == null) {
            //通道实体为空时,清空无效的channelId及appId
            params.put("cid", "");
            params.put("appId", "");
            return;
        }
        Map<String, RmsLog> LOGS = new HashMap<>();
        LOGS.put("Physical", rpsLog);
        LOGS.put("Clients", rcsLog);
        LOGS.put("Commands", rosLog);
        LOGS.put("Keyspace", rksLog);
        WssResponseEntity wre = new WssResponseEntity(ResponseMsgType.RMS_STAT, LOGS);
        wre.setAppId(params.get("appId"));
        WebSocketServer.getInstance().send(JSONObject.toJSONString(wre), che.getChannel());
    }

    private RmsRpsLog GetPhysicalStat(long tm, String ins_id, Map<String, Map<String, String>> sections, Map<String, String> params) {
        RmsRpsLog rpsLog = new RmsRpsLog();
        rpsLog.setId(autoId());
        rpsLog.setTm(new Date(tm));
        rpsLog.setIns_id(ins_id);
        rpsLog.setSector("Physical");
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
        if (params.keySet().containsAll(Arrays.asList("tm", "cpuSu", "cpuUu"))) {
            long pre = Long.parseLong(params.get("tm"));
            double preSu = Double.parseDouble(params.get("cpuSu"));
            double preUu = Double.parseDouble(params.get("cpuUu"));
            double totalNow = rpsLog.getCpu_su() + rpsLog.getCpu_uu();
            double totalPre = preSu + preUu;
            rpsLog.setCpu_per(Math.round((totalNow - totalPre) / ((tm - pre) / 1000.0) * 100) / 100.0);
        } else {
            rpsLog.setCpu_per(0);
        }
        params.put("cpuSu", String.valueOf(rpsLog.getCpu_su()));
        params.put("cpuUu", String.valueOf(rpsLog.getCpu_uu()));
        rpsLog.setIoi(Long.parseLong(stats.get("total_net_input_bytes")));
        rpsLog.setIoo(Long.parseLong(stats.get("total_net_output_bytes")));
        rpsLog.setIo_iik(Double.parseDouble(stats.get("instantaneous_input_kbps")));
        rpsLog.setIo_iok(Double.parseDouble(stats.get("instantaneous_output_kbps")));
        return rpsLog;
    }

    private RmsRosLog GetCommandStat(long tm, String ins_id, Map<String, Map<String, String>> sections, String[] cmd_stat) {
        RmsRosLog rosLog = new RmsRosLog();
        rosLog.setId(autoId());
        rosLog.setTm(new Date(tm));
        rosLog.setIns_id(ins_id);
        rosLog.setSector("Commands");
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

    private RmsRcsLog GetClientStat(long tm, String ins_id, Map<String, Map<String, String>> sections, String[] client_inf) {
        RmsRcsLog rcsLog = new RmsRcsLog();
        rcsLog.setId(autoId());
        rcsLog.setTm(new Date(tm));
        rcsLog.setIns_id(ins_id);
        rcsLog.setSector("Clients");
        Map<String, String> stats = sections.get("Stats");
        Map<String, String> clients = sections.get("Clients");
        rcsLog.setTotal_cons_rec(Long.parseLong(stats.get("total_connections_received")));
        rcsLog.setCli_blo(Integer.parseInt(clients.get("blocked_clients")));
        rcsLog.setCli_con(Integer.parseInt(clients.get("connected_clients")));
        rcsLog.setReject_cons(Long.parseLong(stats.get("rejected_connections")));
        JSONArray joCliArr = new JSONArray();
        for (String inf : client_inf) {
            JSONObject joCli = new JSONObject();
            String[] pat = inf.split(" ");
            for (String kv : pat) {
                String[] kvs = kv.split("=");
                if (kvs.length < 2) {
                    joCli.put(kvs[0], "");
                } else {
                    joCli.put(kvs[0], kvs[1]);
                }

            }
            joCliArr.add(joCli);
        }
        rcsLog.setCli_info(joCliArr.toJSONString());
        joCliArr.clear();
        joCliArr = null;
        return rcsLog;
    }

    public RmsRksLog GetKeysStat(long tm, String ins_id, Map<String, Map<String, String>> sections, Map<String, String> params) {
        RmsRksLog rksLog = new RmsRksLog();
        rksLog.setId(autoId());
        rksLog.setTm(new Date(tm));
        rksLog.setIns_id(ins_id);
        rksLog.setSector("Keyspace");
        Map<String, String> keyspace = sections.get("Keyspace");
        Map<String, String> stats = sections.get("Stats");
        long kc = 0;
        JSONArray joDbs = new JSONArray();
        for (Map.Entry<String, String> entry : keyspace.entrySet()) {
            String[] dbs = entry.getValue().split(",");
            JSONObject joDb = new JSONObject();
            for (String kv : dbs) {
                String[] kvs = kv.split("=");
                joDb.put(kvs[0], kvs[1]);
            }
            joDbs.add(joDb);
            kc += Long.parseLong(dbs[0].split("=")[1]);
        }
        rksLog.setExp_keys(Long.parseLong(stats.get("expired_keys")));
        rksLog.setEvc_keys(Long.parseLong(stats.get("evicted_keys")));
        rksLog.setKsp_hits(Long.parseLong(stats.get("keyspace_hits")));
        rksLog.setKsp_miss(Long.parseLong(stats.get("keyspace_misses")));
        if (params.keySet().containsAll(Arrays.asList("tm", "expKs", "evcKs", "hitKs", "hitKs"))) {
            long pre = Long.parseLong(params.get("tm"));
            long preExp = Long.parseLong(params.get("expKs"));
            long preEvc = Long.parseLong(params.get("evcKs"));
            long preHit = Long.parseLong(params.get("hitKs"));
            long preMiss = Long.parseLong(params.get("missKs"));
            rksLog.setKsp_hits_ps(Math.round((rksLog.getKsp_hits() - preHit) / ((tm - pre) / 1000.0) * 100) / 100.0);
            rksLog.setKsp_miss_ps(Math.round((rksLog.getKsp_miss() - preMiss) / ((tm - pre) / 1000.0) * 100) / 100.0);
            rksLog.setExp_kps(Math.round((rksLog.getExp_keys() - preExp) / ((tm - pre) / 1000.0) * 100) / 100.0);
            rksLog.setEvc_kps(Math.round((rksLog.getEvc_keys() - preEvc) / ((tm - pre) / 1000.0) * 100) / 100.0);
        } else {
            rksLog.setKsp_hits_ps(rksLog.getKsp_hits());
            rksLog.setKsp_miss_ps(rksLog.getKsp_miss());
            rksLog.setExp_kps(rksLog.getExp_keys());
            rksLog.setEvc_kps(rksLog.getEvc_keys());
        }
        params.put("expKs", String.valueOf(rksLog.getExp_keys()));
        params.put("evcKs", String.valueOf(rksLog.getEvc_keys()));
        params.put("hitKs", String.valueOf(rksLog.getKsp_hits()));
        params.put("missKs", String.valueOf(rksLog.getKsp_miss()));
        rksLog.setKey_size(kc);
        rksLog.setDb_info(joDbs.toJSONString());
        joDbs.clear();
        joDbs = null;
        return rksLog;
    }

    private void checkMonitorRule(String rules, RmsLog... logs) {
        String[] ruleList = rules.split(";");
        JSONObject jsonObject = new JSONObject();
        for (RmsLog log : logs) {
            jsonObject.putAll(JSONObject.parseObject(JSONObject.toJSONString(log)));
        }
        for (String rule : ruleList) {
            if (rule.equals("")) {
                continue;
            }
            String[] part = rule.split("\\|");
            String indicator = part[1];
            double value = getValueFromLogs(jsonObject, indicator);
            if (Double.isNaN(value)) {
                continue;
            }
            String unit = part[part.length - 4];
            boolean needMonitor;
            try {
                if (part.length == 11) {
                    String logic = part[5];
                    double valf = unit.equals("percent") ? Double.parseDouble(part[6]) / 100.0 : Double.parseDouble(part[6]);
                    needMonitor = isNeedMonitor(logic, value, valf);
                } else {
                    String logic = part[6];
                    double valf = unit.equals("percent") ? Double.parseDouble(part[5]) / 100.0 : Double.parseDouble(part[5]);
                    double vals = unit.equals("percent") ? Double.parseDouble(part[7]) / 100.0 : Double.parseDouble(part[7]);
                    needMonitor = isNeedMonitor(logic, value, valf, vals);
                }
            } catch (Exception ex) {
                needMonitor = false;
            }
            System.out.println(needMonitor);
        }
        jsonObject.clear();
    }

    private double getValueFromLogs(JSONObject maps, String indicator) {
        if (maps.containsKey(indicator)) {
            return maps.getDoubleValue(indicator);
        }
        return Double.NaN;
    }

    private boolean isNeedMonitor(String logic, double val, double... range) {
        if (range.length == 2 && logic.equals("between")) {
            return val >= range[0] && val <= range[1];
        } else {
            switch (logic) {
                default:
                case "=":
                    return val == range[0];
                case "<":
                    return val < range[0];
                case "<=":
                    return val <= range[0];
                case ">":
                    return val > range[0];
                case ">=":
                    return val >= range[0];
                case "!=":
                    return val != range[0];
            }
        }
    }

    private String autoId() {
        return UUID.randomUUID().toString();
    }
}
