package csnight.redis.monitor.rest.rms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author csnight
 * @description
 * @since 2020-1-12 17:05
 */
@ApiModel(value = "实例配置模型")
public class InsConfigDto {
    @ApiModelProperty(notes = "实例ID", required = true)
    @NotNull
    @NotEmpty
    private String ins_id;
    @NotNull
    @NotEmpty
    @ApiModelProperty(notes = "配置键")
    private String confKey;
    @ApiModelProperty(notes = "配置键值")
    @NotNull
    private String confVal;

    public String getIns_id() {
        return ins_id;
    }

    public void setIns_id(String ins_id) {
        this.ins_id = ins_id;
    }

    public String getConfKey() {
        return confKey;
    }

    public void setConfKey(String confKey) {
        this.confKey = confKey;
    }

    public String getConfVal() {
        return confVal;
    }

    public void setConfVal(String confVal) {
        this.confVal = confVal;
    }
}
