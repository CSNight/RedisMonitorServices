package csnight.redis.monitor.quartz.config;

import com.alibaba.fastjson.annotation.JSONField;
import org.quartz.CronScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.util.Date;

public class CronTriggerConfig implements BaseTriggerConfig {
    private TriggerType triggerType = TriggerType.CronTrigger;
    private String description = "";
    private String identity = "";
    private Date startAt = new Date();
    private String triggerGroup = "";
    private String expression = "";
    private String strategy = "MisfireHandlingInstructionDoNothing";

    public TriggerType getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(TriggerType triggerType) {
        this.triggerType = triggerType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public Date getStartAt() {
        return startAt;
    }

    public void setStartAt(Date startAt) {
        this.startAt = startAt;
    }

    @Override
    public String getTriggerGroup() {
        return triggerGroup;
    }

    public void setTriggerGroup(String triggerGroup) {
        this.triggerGroup = triggerGroup;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    @JSONField(serialize = false)
    @Override
    public Trigger getTrigger() {
        TriggerBuilder<Trigger> t = TriggerBuilder.newTrigger()
                .withDescription(description)
                .withIdentity(identity, triggerGroup)
                .startAt(startAt);

        if (expression.equals("")) {
            return null;
        }
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(expression);
        switch (strategy) {
            case "MisfireHandlingInstructionIgnoreMisfires":
                cronScheduleBuilder = cronScheduleBuilder.withMisfireHandlingInstructionIgnoreMisfires();
                break;
            case "MisfireHandlingInstructionFireAndProceed":
                cronScheduleBuilder = cronScheduleBuilder.withMisfireHandlingInstructionFireAndProceed();
                break;
            default:
            case "MisfireHandlingInstructionDoNothing":
                cronScheduleBuilder = cronScheduleBuilder.withMisfireHandlingInstructionDoNothing();
                break;
        }
        return t.withSchedule(cronScheduleBuilder).build();
    }
}
