package com.redis.monitor.quartz.config;

import org.quartz.Trigger;

public interface BaseTriggerConfig {
    public String getIdentity();
    public String getTriggerGroup();
    public Trigger getTrigger();
}
