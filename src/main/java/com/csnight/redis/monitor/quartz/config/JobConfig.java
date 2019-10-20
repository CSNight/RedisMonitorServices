package com.csnight.redis.monitor.quartz.config;

import com.csnight.redis.monitor.utils.GUID;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class JobConfig {
    @ApiModelProperty(notes = "工作名称", hidden = true)
    private String jobName = GUID.getUUID();
    @ApiModelProperty(notes = "工作组")
    private String jobGroup;
    @ApiModelProperty(notes = "工作参数")
    private Object invokeParam;
    @ApiModelProperty(notes = "任务描述")
    private String description;
    @ApiModelProperty(notes = "触发器配置JSONObject")
    private String triggerConfig = "";
    @ApiModelProperty(notes = "触发器类型 Simple=0,Cron=1,CalendarInterval=3,DailyTimeInterval=4")
    private int triggerType = 0;

    public int getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(int triggerType) {
        this.triggerType = triggerType;
    }

    public String getTriggerConfig() {
        return triggerConfig;
    }

    public void setTriggerConfig(String triggerConfig) {
        this.triggerConfig = triggerConfig;
    }


    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobGroup() {
        return jobGroup;
    }

    public void setJobGroup(String jobGroup) {
        this.jobGroup = jobGroup;
    }

    public Object getInvokeParam() {
        return invokeParam;
    }

    public void setInvokeParam(Object invokeParam) {
        this.invokeParam = invokeParam;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
