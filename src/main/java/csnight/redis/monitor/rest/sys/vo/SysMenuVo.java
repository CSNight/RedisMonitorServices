package csnight.redis.monitor.rest.sys.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "角色菜单更新模型")
public class SysMenuVo {
    @ApiModelProperty(notes = "角色菜单ID")
    private Long id;
    @ApiModelProperty(notes = "角色菜单父ID")
    private Long pid;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }
}
