package csnight.redis.monitor.busi.task;

import csnight.redis.monitor.db.jpa.RmsMonitorRule;
import csnight.redis.monitor.db.repos.RmsInsRepository;
import csnight.redis.monitor.db.repos.RmsJobRepository;
import csnight.redis.monitor.db.repos.RmsMonRuleRepository;
import csnight.redis.monitor.quartz.JobFactory;
import csnight.redis.monitor.rest.task.dto.MonitorRuleDto;
import csnight.redis.monitor.utils.BaseUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class MonitorRuleManagerImpl {
    @Resource
    private RmsJobRepository jobRepository;
    @Resource
    private RmsInsRepository insRepository;
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
        rule.setCreate_time(new Date());
        rule.setCreate_user(BaseUtils.GetUserFromContext());
        if (checkRuleConflict(rule)) {
            ruleRepository.save(rule);
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
            if (checkRuleConflict(ruleOrigin)) {
                return ruleRepository.save(ruleOrigin);
            }
        }
        return null;
    }

    public String DeleteMonitorRule(String id) {
        if (ruleRepository.existsById(id)) {
            ruleRepository.deleteById(id);
            return "success";
        }
        return "failed";
    }

    private boolean checkRuleConflict(RmsMonitorRule rule) {
        boolean isValid = true;
        RmsMonitorRule ruleExist = ruleRepository.findByExpressionAndIns(rule.getExpression(), rule.getIns());
        if (ruleExist != null) {
            isValid = false;
        }
        return isValid;
    }
}
