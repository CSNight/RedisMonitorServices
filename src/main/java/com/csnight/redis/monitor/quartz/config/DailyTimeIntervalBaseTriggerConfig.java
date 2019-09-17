package com.csnight.redis.monitor.quartz.config;

import org.quartz.DailyTimeIntervalScheduleBuilder;
import org.quartz.DateBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.util.Date;

public class DailyTimeIntervalBaseTriggerConfig implements BaseTriggerConfig {
    private TriggerType triggerType = TriggerType.DailyTimeIntervalTrigger;
    private String description = "";
    private String identity = "";
    private Date startAt = new Date();
    private String jobGroup = "";
    private DateBuilder.IntervalUnit unit = DateBuilder.IntervalUnit.SECOND;
    private int interval = 1;

    @Override
    public Trigger getTrigger() {
        TriggerBuilder t = TriggerBuilder.newTrigger()
                .withDescription(description)
                .withIdentity(identity, jobGroup)
                .startAt(startAt);
        Trigger trigger = t.withSchedule(DailyTimeIntervalScheduleBuilder.dailyTimeIntervalSchedule().withInterval(interval, unit)
                .withMisfireHandlingInstructionFireAndProceed()).build();
        return null;
    }
}
