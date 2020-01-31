package csnight.redis.monitor.rest.task.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@ApiModel(value = "任务配置模型")
public class TaskConfDto {
    @NotNull
    @NotEmpty
    @ApiModelProperty(notes = "实例ID", hidden = true)
    private String ins_id;
    @NotNull
    @NotEmpty
    @ApiModelProperty(notes = "用户ID", hidden = true)
    private String uid;
    @NotNull
    @NotEmpty
    @ApiModelProperty(notes = "工作名称", hidden = true)
    private String jobName = "";
    @NotNull
    @NotEmpty
    @ApiModelProperty(notes = "工作组")
    private String jobGroup;
    @ApiModelProperty(notes = "工作参数")
    private Object invokeParam;
    @ApiModelProperty(notes = "任务描述")
    private String description;
    @NotNull
    @NotEmpty
    @ApiModelProperty(notes = "触发器配置JSONObject")
    private String triggerConfig = "";
    @ApiModelProperty(notes = "触发器类型 Simple=0,Cron=1,CalendarInterval=3,DailyTimeInterval=4")
    private int triggerType = 0;

    public String getIns_id() {
        return ins_id;
    }

    public void setIns_id(String ins_id) {
        this.ins_id = ins_id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public String getTriggerConfig() {
        return triggerConfig;
    }

    public void setTriggerConfig(String triggerConfig) {
        this.triggerConfig = triggerConfig;
    }

    public int getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(int triggerType) {
        this.triggerType = triggerType;
    }
}
