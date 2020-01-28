package csnight.redis.monitor.rest.sys.dto;

import csnight.redis.monitor.db.jpa.SysMenu;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

@ApiModel(value = "权限模型")
public class PermissionDto {
    @ApiModelProperty(notes = "权限ID")
    private String id;
    @NotNull
    @NotEmpty
    @ApiModelProperty(notes = "权限名称", required = true)
    private String name;
    @NotNull
    @ApiModelProperty(notes = "所属菜单", required = true)
    private SysMenu menu;
    @ApiModelProperty(notes = "权限描述")
    private String description;
    @ApiModelProperty(notes = "创建用户")
    private String create_user;
    @ApiModelProperty(notes = "创建时间")
    private Date create_time;

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

    public SysMenu getMenu() {
        return menu;
    }

    public void setMenu(SysMenu menu) {
        this.menu = menu;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreate_user() {
        return create_user;
    }

    public void setCreate_user(String create_user) {
        this.create_user = create_user;
    }

    public Date getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Date create_time) {
        this.create_time = create_time;
    }
}
