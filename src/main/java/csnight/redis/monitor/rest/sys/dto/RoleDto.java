package csnight.redis.monitor.rest.sys.dto;

import csnight.redis.monitor.db.jpa.SysPermission;
import csnight.redis.monitor.rest.sys.vo.SysMenuVo;
import io.swagger.annotations.ApiModel;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Set;
@ApiModel(value = "角色模型")
public class RoleDto {

    private String id;
    @NotEmpty
    @NotNull
    private String code;
    @NotEmpty
    @NotNull
    private String name;
    @Min(0)
    @Max(3)
    private int level;

    private Date create_time;

    private Set<SysPermission> permissionSet;
    private Set<SysMenuVo> menuSet;

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
}
