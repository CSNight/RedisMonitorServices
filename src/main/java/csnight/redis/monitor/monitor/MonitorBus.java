package csnight.redis.monitor.monitor;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import csnight.redis.monitor.db.jpa.RmsMonitorRule;
import csnight.redis.monitor.db.repos.RmsJobRepository;
import csnight.redis.monitor.db.repos.RmsMonRuleRepository;
import csnight.redis.monitor.utils.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MonitorBus {
    private Logger _log = LoggerFactory.getLogger(MonitorBus.class);
    private LoadingCache<String, String> counter = CacheBuilder.newBuilder()
            .expireAfterWrite(10 * 60, TimeUnit.SECONDS)
            .build(new CacheLoader<>() {
                @Override
                public String load(String s) throws Exception {
                    _log.error("Job rule for " + s + " refresh");
                    return getEnableRule(s);
                }
            });
    private RmsMonRuleRepository ruleRepository;
    private RmsJobRepository jobRepository;
    Map<String, List<String>> relations = new ConcurrentHashMap<>();
    Map<String, RedisMonitor> monitors = new ConcurrentHashMap<>();
    Map<String, RmsMonitorRule> rules = new ConcurrentHashMap<>();
    private static MonitorBus ourInstance;

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
        jobRepository = ReflectUtils.getBean(RmsJobRepository.class);
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
    }

    public String getEnableRule(String jobKey) {
        String ens = "";
        List<String> enableRule = new ArrayList<>();
        if (relations.containsKey(jobKey)) {
            List<String> rs = relations.get(jobKey);
            for (String rid : rs) {
                if (monitors.containsKey(rid)) {
                    continue;
                }
                RmsMonitorRule rule = rules.get(rid);
                if (!rule.isEnabled()) {
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

    public void registerRuleToJob(String jobKey, String rid, RmsMonitorRule rule) {
        if (!monitors.containsKey(jobKey)) {
            List<String> rs = new ArrayList<>();
            rs.add(rid);
            relations.put(jobKey, rs);
        }
        rule.setEnabled(true);
        rules.put(rid, rule);
        counter.refresh(jobKey);
    }

    public void unregisterRuleForJob(String jobKey, String rid) {
        if (relations.containsKey(jobKey)) {
            List<String> rs = relations.get(jobKey);
            if (monitors.containsKey(rid)) {
                monitors.get(rid).destroy();
                monitors.remove(rid);
                rs.remove(rid);
            }
        }
        rules.remove(rid);
        counter.refresh(jobKey);
    }

    public void destroy() {
        Set<String> keys = monitors.keySet();
        for (String key : keys) {
            monitors.get(key).destroy();
        }
        relations.clear();
        monitors.clear();
        Set<String> rids = rules.keySet();
        for (String rid : rids) {
            rules.get(rid).setEnabled(false);
        }
        ruleRepository.saveAll(rules.values());
        rules.clear();
        counter.cleanUp();
    }
}
