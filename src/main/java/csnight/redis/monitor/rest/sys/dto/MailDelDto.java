package csnight.redis.monitor.rest.sys.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.HashSet;
import java.util.Set;

@ApiModel(value = "邮件记录删除模型")
public class MailDelDto {
    @ApiModelProperty(notes = "待删除ID集")
    private Set<String> ids = new HashSet<>();

    public Set<String> getIds() {
        return ids;
    }

    public void setIds(Set<String> ids) {
        this.ids = ids;
    }
}
