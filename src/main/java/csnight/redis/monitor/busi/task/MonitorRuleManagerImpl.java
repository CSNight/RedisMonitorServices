package csnight.redis.monitor.busi.task;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.db.jpa.RmsJobInfo;
import csnight.redis.monitor.db.jpa.RmsMonitorRule;
import csnight.redis.monitor.db.jpa.SysMailConfig;
import csnight.redis.monitor.db.repos.RmsJobRepository;
import csnight.redis.monitor.db.repos.RmsMonRuleRepository;
import csnight.redis.monitor.db.repos.SysMailConfRepository;
import csnight.redis.monitor.db.repos.SysUserRepository;
import csnight.redis.monitor.monitor.MonitorBus;
import csnight.redis.monitor.quartz.JobFactory;
import csnight.redis.monitor.rest.task.dto.MonitorRuleDto;
import csnight.redis.monitor.utils.BaseUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MonitorRuleManagerImpl {
    @Resource
    private RmsJobRepository jobRepository;
    @Resource
    private SysUserRepository userRepository;
    @Resource
    private SysMailConfRepository mailConfRepository;
    @Resource
    private RmsMonRuleRepository ruleRepository;
    @Resource
    private JobFactory jobFactory;

    public List<RmsMonitorRule> GetAllMonitorRule() {
        return ruleRepository.findAll();
    }

    public List<RmsMonitorRule> GetUserRules() {
        String user = BaseUtils.GetUserFromContext();
        return ruleRepository.findByUser(user);
    }

    public RmsMonitorRule AddMonitorRule(MonitorRuleDto dto) {
        RmsJobInfo jobInfo = checkStatJobExist(dto.getJob_id());
        if (jobInfo == null) {
            return null;
        }
        String user = BaseUtils.GetUserFromContext();
        String userId = userRepository.findIdByUsername(user);
        if (!checkEmailConf(userId)) {
            return null;
        }
        RmsMonitorRule rule = new RmsMonitorRule();
        rule.setIns(dto.getJob_id());
        rule.setName(dto.getName());
        rule.setDescription(dto.getDescription());
        rule.setIndicator(dto.getIndicator());
        rule.setCycle(dto.getCycle());
        rule.setDuration(dto.getDuration());
        rule.setSign(dto.getSign());
        rule.setExpression(dto.getExpression());
        rule.setClazz(dto.getClazz());
        rule.setContact(dto.getContact());
        rule.setSubject(dto.getSubject());
        rule.setEnabled(false);
        rule.setCreate_time(new Date());
        rule.setCreate_user(user);
        if (checkRuleConflict(rule)) {
            return ruleRepository.save(rule);
        }
        return rule;
    }

    public RmsMonitorRule UpdateMonitorRule(MonitorRuleDto dto) {
        Optional<RmsMonitorRule> optRule = ruleRepository.findById(dto.getId());
        if (optRule.isPresent()) {
            RmsMonitorRule ruleOrigin = optRule.get();
            ruleOrigin.setIns(dto.getJob_id());
            ruleOrigin.setName(dto.getName());
            ruleOrigin.setDescription(dto.getDescription());
            ruleOrigin.setIndicator(dto.getIndicator());
            ruleOrigin.setCycle(dto.getCycle());
            ruleOrigin.setDuration(dto.getDuration());
            ruleOrigin.setSign(dto.getSign());
            ruleOrigin.setExpression(dto.getExpression());
            ruleOrigin.setClazz(dto.getClazz());
            ruleOrigin.setContact(dto.getContact());
            ruleOrigin.setSubject(dto.getSubject());
            ruleOrigin.setEnabled(false);
            if (checkRuleConflict(ruleOrigin)) {
                return ruleRepository.save(ruleOrigin);
            }
        }
        return null;
    }

    public String DeleteMonitorRule(String id) {
        Optional<RmsMonitorRule> optRule = ruleRepository.findById(id);
        if (optRule.isPresent()) {
            RmsMonitorRule rule = optRule.get();
            MonitorBus.getIns().unregisterRuleForJob(rule.getIns(), rule.getId());
            ruleRepository.deleteById(id);
            return "success";
        }
        return "failed";
    }

    private boolean injectStatJob(RmsJobInfo jobInfo, RmsMonitorRule rule) {
        Object ruleObj = jobFactory.GetJobRule(jobInfo.getJob_name(), jobInfo.getJob_group());
        if (ruleObj instanceof Map) {
            Map<String, String> rules = (Map<String, String>) ruleObj;
            rules.put(rule.getId(), JSONObject.toJSONString(rule));
            return jobFactory.SetJobRule(jobInfo.getJob_name(), jobInfo.getJob_group(), rules);
        }
        return false;
    }

    private boolean checkRuleConflict(RmsMonitorRule rule) {
        boolean isValid = true;
        RmsMonitorRule ruleExist = ruleRepository.findByExpressionAndIns(rule.getExpression(), rule.getIns());
        if (ruleExist != null) {
            isValid = false;
        }
        return isValid;
    }

    private boolean checkEmailConf(String userId) {
        SysMailConfig mailConfig = mailConfRepository.findByUid(userId);
        return mailConfig != null;

    }

    private RmsJobInfo checkStatJobExist(String jobId) {
        return jobRepository.findByJobName(jobId);
    }
}
