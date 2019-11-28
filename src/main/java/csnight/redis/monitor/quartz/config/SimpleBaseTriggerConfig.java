package csnight.redis.monitor.quartz.config;

import org.quartz.DateBuilder;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.util.Date;

public class SimpleBaseTriggerConfig implements BaseTriggerConfig {
    private TriggerType triggerType = TriggerType.SimpleTrigger;
    private String description = "";
    private String identity = "";
    private Date startAt = new Date();
    private String triggerGroup = "";
    private int interval = 1;
    private DateBuilder.IntervalUnit unit = DateBuilder.IntervalUnit.SECOND;
    private int repeatCount = -1;
    private String strategy = "";

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public DateBuilder.IntervalUnit getUnit() {
        return unit;
    }

    public void setUnit(DateBuilder.IntervalUnit unit) {
        this.unit = unit;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

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

    @Override
    public Trigger getTrigger() {
        TriggerBuilder t = TriggerBuilder.newTrigger()
                .withDescription(description)
                .withIdentity(identity, triggerGroup)
                .startAt(startAt);
        SimpleScheduleBuilder simpleScheduleBuilder = null;
        switch (unit) {
            default:
            case SECOND:
                if (repeatCount == -1) {
                    simpleScheduleBuilder = SimpleScheduleBuilder.repeatSecondlyForever(interval).repeatForever();
                } else {
                    simpleScheduleBuilder = SimpleScheduleBuilder.repeatSecondlyForTotalCount(repeatCount, interval);
                }
                break;
            case MINUTE:
                if (repeatCount == -1) {
                    simpleScheduleBuilder = SimpleScheduleBuilder.repeatMinutelyForever(interval).repeatForever();
                } else {
                    simpleScheduleBuilder = SimpleScheduleBuilder.repeatHourlyForTotalCount(repeatCount, interval);
                }
                break;
            case HOUR:
                if (repeatCount == -1) {
                    simpleScheduleBuilder = SimpleScheduleBuilder.repeatHourlyForever(interval).repeatForever();
                } else {
                    simpleScheduleBuilder = SimpleScheduleBuilder.repeatHourlyForTotalCount(repeatCount, interval);
                }
                break;
        }
        switch (strategy) {
            case "MisfireHandlingInstructionFireNow":
                simpleScheduleBuilder = simpleScheduleBuilder.withMisfireHandlingInstructionFireNow();
                break;
            default:
            case "MisfireHandlingInstructionIgnoreMisfires":
                simpleScheduleBuilder = simpleScheduleBuilder.withMisfireHandlingInstructionIgnoreMisfires();
                break;
            case "MisfireHandlingInstructionNextWithExistingCount":
                simpleScheduleBuilder = simpleScheduleBuilder.withMisfireHandlingInstructionNextWithExistingCount();
                break;
            case "MisfireHandlingInstructionNowWithExistingCount":
                simpleScheduleBuilder = simpleScheduleBuilder.withMisfireHandlingInstructionNowWithExistingCount();
                break;
            case "MisfireHandlingInstructionNextWithRemainingCount":
                simpleScheduleBuilder = simpleScheduleBuilder.withMisfireHandlingInstructionNextWithRemainingCount();
                break;
            case "MisfireHandlingInstructionNowWithRemainingCount":
                simpleScheduleBuilder = simpleScheduleBuilder.withMisfireHandlingInstructionNowWithRemainingCount();
                break;
        }
        return t.withSchedule(simpleScheduleBuilder).build();
    }
}
