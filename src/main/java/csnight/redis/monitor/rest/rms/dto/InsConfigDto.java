package csnight.redis.monitor.rest.rms.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author csnight
 * @description
 * @since 2020-1-12 17:05
 */
public class InsConfigDto {
    @NotNull
    @NotEmpty
    private String ins_id;
    @NotNull
    @NotEmpty
    private String confKey;
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
