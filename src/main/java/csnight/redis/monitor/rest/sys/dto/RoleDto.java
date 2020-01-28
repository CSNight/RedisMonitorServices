package csnight.redis.monitor.rest.sys.dto;

import csnight.redis.monitor.db.jpa.SysCommands;
import csnight.redis.monitor.db.jpa.SysPermission;
import csnight.redis.monitor.rest.sys.vo.SysMenuVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Set;

@ApiModel(value = "角色模型")
public class RoleDto {
    @ApiModelProperty(notes = "角色ID")
    private String id;
    @NotEmpty
    @NotNull
    @ApiModelProperty(notes = "角色编码", required = true)
    private String code;
    @NotEmpty
    @NotNull
    @ApiModelProperty(notes = "角色名称", required = true)
    private String name;
    @Min(0)
    @Max(3)
    @ApiModelProperty(notes = "角色级别", example = "0")
    private int level;
    @ApiModelProperty(notes = "角色创建时间")
    private Date create_time;
    @ApiModelProperty(notes = "角色关联权限")
    private Set<SysPermission> permissionSet;
    @ApiModelProperty(notes = "角色关联菜单")
    private Set<SysMenuVo> menuSet;
    @ApiModelProperty(notes = "角色关联命令")
    private SysCommands commands;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Set<SysPermission> getPermissionSet() {
        return permissionSet;
    }

    public void setPermissionSet(Set<SysPermission> permissionSet) {
        this.permissionSet = permissionSet;
    }

    public Set<SysMenuVo> getMenuSet() {
        return menuSet;
    }

    public void setMenuSet(Set<SysMenuVo> menuSet) {
        this.menuSet = menuSet;
    }

    public Date getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Date create_time) {
        this.create_time = create_time;
    }

    public SysCommands getCommands() {
        return commands;
    }

    public void setCommands(SysCommands commands) {
        this.commands = commands;
    }
}
