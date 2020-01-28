package csnight.redis.monitor.rest.rms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author csnight
 * @description
 * @since 2020-1-18 21:59
 */
@ApiModel(value = "shake操作配置模型")
public class ShakeConfDto {
    @NotNull
    @NotEmpty
    @ApiModelProperty(notes = "shake操作配置")
    private String configs;
    @NotNull
    @NotEmpty
    @ApiModelProperty(notes = "shake操作类型")
    private String type;

    public String getConfigs() {
        return configs;
    }

    public void setConfigs(String configs) {
        this.configs = configs;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
