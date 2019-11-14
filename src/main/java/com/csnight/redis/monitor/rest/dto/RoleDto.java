package com.csnight.redis.monitor.rest.dto;

import com.csnight.redis.monitor.db.jpa.SysMenu;
import com.csnight.redis.monitor.db.jpa.SysPermission;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

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

    private Set<SysPermission> permissionSet;
    private Set<SysMenu> menuSet;

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

    public Set<SysMenu> getMenuSet() {
        return menuSet;
    }

    public void setMenuSet(Set<SysMenu> menuSet) {
        this.menuSet = menuSet;
    }
}
