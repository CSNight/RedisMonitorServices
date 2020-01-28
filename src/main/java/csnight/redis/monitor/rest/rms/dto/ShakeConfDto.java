package csnight.redis.monitor.rest.rms.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author csnight
 * @description
 * @since 2020-1-18 21:59
 */
public class ShakeConfDto {
    @NotNull
    @NotEmpty
    private String configs;
    @NotNull
    @NotEmpty
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
