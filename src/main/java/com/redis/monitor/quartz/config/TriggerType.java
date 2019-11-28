package com.redis.monitor.quartz.config;

public enum TriggerType {
    SimpleTrigger(0),
    CronTrigger(1),
    CalendarIntervalTrigger(2),
    DailyTimeIntervalTrigger(3);

    TriggerType(int i) {
    }
}
