package csnight.redis.monitor.monitor;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import csnight.redis.monitor.db.jpa.RmsMonitorRule;
import csnight.redis.monitor.db.repos.RmsMonRuleRepository;
import csnight.redis.monitor.utils.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class MonitorBus {
    private Logger _log = LoggerFactory.getLogger(MonitorBus.class);
    private LoadingCache<String, String> counter = CacheBuilder.newBuilder()
            .expireAfterWrite(60 * 60 * 24, TimeUnit.SECONDS)
            .build(new CacheLoader<>() {
                @Override
                public String load(String s) throws Exception {
                    _log.warn("Job rule for " + s + " refresh");
                    return getEnableRule(s);
                }
            });
    private RmsMonRuleRepository ruleRepository;
    Map<String, List<String>> relations = new ConcurrentHashMap<>();
    Map<String, RedisMonitor> monitors = new ConcurrentHashMap<>();
    Map<String, RmsMonitorRule> rules = new ConcurrentHashMap<>();
    Map<String, String> blackList = new ConcurrentHashMap<>();
    private static MonitorBus ourInstance;
    private ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);

    public static MonitorBus getIns() {
        if (ourInstance == null) {
            synchronized (MonitorBus.class) {
                if (ourInstance == null) {
                    ourInstance = new MonitorBus();
                }
            }
        }
        return ourInstance;
    }

    private MonitorBus() {
        ruleRepository = ReflectUtils.getBean(RmsMonRuleRepository.class);
        initialize();
    }

    private void initialize() {
        List<RmsMonitorRule> monitorRules = ruleRepository.findAll();
        for (RmsMonitorRule monitorRule : monitorRules) {
            if (relations.containsKey(monitorRule.getIns())) {
                relations.get(monitorRule.getIns()).add(monitorRule.getId());
            } else {
                List<String> rule = new ArrayList<>();
                rule.add(monitorRule.getId());
                relations.put(monitorRule.getIns(), rule);
            }
            rules.put(monitorRule.getId(), monitorRule);
        }
        scheduledThreadPool.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            List<String> dels = new ArrayList<>();
            Collection<String> keys = blackList.keySet();
            for (String key : keys) {
                String[] delayInfo = blackList.get(key).split(",");
                long st = Long.parseLong(delayInfo[0]);
                int delay = Integer.parseInt(delayInfo[1]);
                if ((now - st) > delay) {
                    dels.add(key);
                }
            }
            for (String k : dels) {
                blackList.remove(k);
            }
            dels.clear();
            dels = null;
        }, 1000, 5000, TimeUnit.MILLISECONDS);
    }

    public String getEnableRule(String jobKey) {
        String ens;
        List<String> enableRule = new ArrayList<>();
        if (relations.containsKey(jobKey)) {
            List<String> rs = relations.get(jobKey);
            for (String rid : rs) {
                RmsMonitorRule rule = rules.get(rid);
                if (rule == null || !rule.isEnabled()) {
                    continue;
                }
                String r = rule.getId() + "|" + rule.getExpression() + "|" + rule.getClazz()
                        + "|" + rule.getContact() + "|" + rule.getSubject();
                enableRule.add(r);
            }
        }
        ens = String.join(";", enableRule);
        enableRule.clear();
        enableRule = null;
        return ens;
    }

    public String GetEnableByCache(String jobKey) {
        try {
            return counter.get(jobKey);
        } catch (ExecutionException e) {
            return "";
        }
    }

    public void registerRuleToJob(String jobKey, RmsMonitorRule rule) {
        if (!relations.containsKey(jobKey)) {
            List<String> rs = new ArrayList<>();
            rs.add(rule.getId());
            relations.put(jobKey, rs);
        } else {
            relations.get(jobKey).add(rule.getId());
        }
        rules.put(rule.getId(), rule);
        counter.refresh(jobKey);
    }

    public void unregisterRuleForJob(String jobKey, String rid) {
        if (relations.containsKey(jobKey)) {
            List<String> rs = relations.get(jobKey);
            if (monitors.containsKey(rid)) {
                monitors.get(rid).destroy();
                monitors.remove(rid);
            }
            blackList.remove(rid);
            System.out.println(blackList.size());
            rs.remove(rid);
        }
        rules.remove(rid);
        counter.refresh(jobKey);
    }

    public void toggleRule(String jobKey, RmsMonitorRule rule, boolean state) {
        if (rules.containsKey(rule.getId())) {
            if (!state) {
                if (monitors.containsKey(rule.getId())) {
                    monitors.get(rule.getId()).destroy();
                    monitors.remove(rule.getId());
                }
                blackList.remove(rule.getId());
                System.out.println(blackList.size());
            }
            rules.put(rule.getId(), rule);
        }
        counter.refresh(jobKey);
    }

    //注册统计任务关联的监控规则到监控总线，并启用已经存在的监控规则
    public void registerJobRules(String jobKey) {
        List<RmsMonitorRule> ruleList = ruleRepository.findByIns(jobKey);
        List<String> rs = ruleList.stream().map(RmsMonitorRule::getId).collect(Collectors.toList());
        relations.put(jobKey, rs);
        counter.refresh(jobKey);
    }

    //销毁统计任务关联的所有监控器并禁用监控规则
    public void unregisterJobRules(String jobKey) {
        if (relations.containsKey(jobKey)) {
            List<String> rs = relations.get(jobKey);
            for (String rid : rs) {
                blackList.remove(rid);
                System.out.println(blackList.size());
                RedisMonitor monitor = monitors.get(rid);
                if (monitor != null) {
                    monitor.destroy();
                    monitors.remove(rid);
                }
            }
            rs.clear();
            relations.remove(jobKey);
            counter.invalidate(jobKey);
        }
    }

    public void addMonitor(String rule, String uid, String ins_id) {
        String[] parts = rule.split("\\|");
        if (!monitors.containsKey(parts[0])) {
            RmsMonitorRule ruleIns = rules.get(parts[0]);
            if (ruleIns == null) {
                return;
            }
            RedisMonitor monitor = new RedisMonitorImpl(rule, uid, ins_id);
            monitor.setDelay(ruleIns.getDelay());
            monitors.put(parts[0], monitor);
        }
    }

    public RedisMonitor getMonitor(String rule) {
        String[] parts = rule.split("\\|");
        return monitors.get(parts[0]);
    }

    public MonitorState getMonitorState(String rule) {
        String[] parts = rule.split("\\|");
        if (blackList.containsKey(parts[0])) {
            return MonitorState.JAILING;
        }
        RedisMonitor monitor = monitors.get(parts[0]);
        if (monitor != null) {
            return monitor.getState();
        }
        return MonitorState.NOTFOUND;
    }

    public void destroyMonitor(String rule) {
        String[] parts = rule.split("\\|");
        RedisMonitor monitor = monitors.get(parts[0]);
        if (monitor != null) {
            if (monitor.getDelay() != -1) {
                blackList.put(parts[0], System.currentTimeMillis() + "," + monitor.getDelay() * 1000);
            }
            monitor.destroy();
            monitors.remove(parts[0]);
        }
        System.gc();
    }

    public void destroyMonitorForJob(String jobKey) {
        if (relations.containsKey(jobKey)) {
            List<String> rs = relations.get(jobKey);
            for (String rid : rs) {
                blackList.remove(rid);
                System.out.println(blackList.size());
                RedisMonitor monitor = monitors.get(rid);
                if (monitor != null) {
                    monitor.destroy();
                    monitors.remove(rid);
                }
            }
        }
    }

    public void destroy() {
        scheduledThreadPool.shutdownNow();
        Set<String> keys = monitors.keySet();
        for (String key : keys) {
            monitors.get(key).destroy();
        }
        relations.clear();
        monitors.clear();
        rules.clear();
        blackList.clear();
        counter.invalidateAll();
    }
}
