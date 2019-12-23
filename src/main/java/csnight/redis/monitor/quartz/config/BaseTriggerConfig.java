package csnight.redis.monitor.quartz.config;

import org.quartz.Trigger;

public interface BaseTriggerConfig {
    String getIdentity();

    String getTriggerGroup();

    Trigger getTrigger();
}
