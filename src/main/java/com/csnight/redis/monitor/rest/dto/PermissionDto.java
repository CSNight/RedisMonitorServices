package com.csnight.redis.monitor.rest.dto;

import com.csnight.redis.monitor.db.jpa.SysMenu;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

public class PermissionDto {
    private String id;
    @NotNull
    @NotEmpty
    private String name;
    @NotNull
    private SysMenu menu;
    private String description;
    private String access_role;
    private String create_user;
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

    public String getAccess_role() {
        return access_role;
    }

    public void setAccess_role(String access_role) {
        this.access_role = access_role;
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
