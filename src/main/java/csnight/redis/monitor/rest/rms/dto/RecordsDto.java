package csnight.redis.monitor.rest.rms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.HashSet;
import java.util.Set;

@ApiModel(value = "记录集模型")
public class RecordsDto {
    @ApiModelProperty(notes = "记录ID集")
    private Set<String> ids = new HashSet<>();

    public Set<String> getIds() {
        return ids;
    }

    public void setIds(Set<String> ids) {
        this.ids = ids;
    }
}
