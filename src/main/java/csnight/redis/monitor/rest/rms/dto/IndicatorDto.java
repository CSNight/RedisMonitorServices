package csnight.redis.monitor.rest.rms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@ApiModel(value = "监控指标配置模型")
public class IndicatorDto {
    private String id;
    @ApiModelProperty(notes = "指标值", required = true)
    @NotNull
    @NotEmpty
    private String name;
    @ApiModelProperty(notes = "指标名称", required = true)
    @NotNull
    @NotEmpty
    private String label;
    @ApiModelProperty(notes = "支持信号量", required = true)
    @NotNull
    @NotEmpty
    private String sign_support;
    @ApiModelProperty(notes = "支持表达式", required = true)
    @NotNull
    @NotEmpty
    private String exp_support;
    @ApiModelProperty(notes = "单位", required = true)
    @NotNull
    @NotEmpty
    private String unit;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getSign_support() {
        return sign_support;
    }

    public void setSign_support(String sign_support) {
        this.sign_support = sign_support;
    }

    public String getExp_support() {
        return exp_support;
    }

    public void setExp_support(String exp_support) {
        this.exp_support = exp_support;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
