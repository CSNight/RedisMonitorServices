package csnight.redis.monitor.rest.task.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@ApiModel(value = "监控规则模型")
public class MonitorRuleDto {
    private String id;
    @ApiModelProperty(notes = "关联任务", required = true)
    @NotNull
    @NotEmpty
    private String job_id;
    @ApiModelProperty(notes = "规则名称", required = true)
    @NotNull
    @NotEmpty
    private String name;
    @ApiModelProperty(notes = "规则描述", required = true)
    @NotNull
    @NotEmpty
    private String description;
    @ApiModelProperty(notes = "指标值", required = true)
    @NotNull
    @NotEmpty
    private String indicator;
    @ApiModelProperty(notes = "信号量", required = true)
    @NotNull
    @NotEmpty
    private String sign;
    @ApiModelProperty(notes = "表达式", required = true)
    @NotNull
    @NotEmpty
    private String expression;
    @ApiModelProperty(notes = "通知级别", required = true)
    @NotNull
    @NotEmpty
    private String clazz;
    @ApiModelProperty(notes = "联系人", required = true)
    @NotNull
    @NotEmpty
    private String contact;
    @ApiModelProperty(notes = "邮件主题", required = true)
    @NotNull
    @NotEmpty
    private String subject;
    @ApiModelProperty(notes = "多次通知间隔", required = true)
    @Min(-1)
    @Max(3600 * 24)
    private int delay;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJob_id() {
        return job_id;
    }

    public void setJob_id(String job_id) {
        this.job_id = job_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIndicator() {
        return indicator;
    }

    public void setIndicator(String indicator) {
        this.indicator = indicator;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }
}
