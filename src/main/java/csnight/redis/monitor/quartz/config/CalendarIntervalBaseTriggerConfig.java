package csnight.redis.monitor.quartz.config;

import com.alibaba.fastjson.annotation.JSONField;
import org.quartz.CalendarIntervalScheduleBuilder;
import org.quartz.DateBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.util.Date;

public class CalendarIntervalBaseTriggerConfig implements BaseTriggerConfig {
    private TriggerType triggerType = TriggerType.CalendarIntervalTrigger;
    private String description = "";
    private String identity = "";
    private Date startAt = new Date();
    private String triggerGroup = "";
    private DateBuilder.IntervalUnit unit = DateBuilder.IntervalUnit.SECOND;
    private int interval = 1;
    private String strategy = "";

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

    public String getTriggerGroup() {
        return triggerGroup;
    }

    public void setTriggerGroup(String triggerGroup) {
        this.triggerGroup = triggerGroup;
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
        CalendarIntervalScheduleBuilder builder = CalendarIntervalScheduleBuilder.calendarIntervalSchedule().withInterval(interval, unit);
        switch (strategy) {
            case "MisfireHandlingInstructionFireAndProceed":
                builder = builder.withMisfireHandlingInstructionFireAndProceed();
                break;
            default:
            case "MisfireHandlingInstructionDoNothing":
                builder = builder.withMisfireHandlingInstructionDoNothing();
                break;
            case "MisfireHandlingInstructionIgnoreMisfires":
                builder = builder.withMisfireHandlingInstructionIgnoreMisfires();
                break;
        }
        return t.withSchedule(builder).build();
    }
}
