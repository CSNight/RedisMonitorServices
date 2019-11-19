package com.csnight.redis.monitor.rest.dto;

import com.csnight.redis.monitor.db.jpa.SysRole;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.*;
import java.util.HashSet;
import java.util.Set;

@ApiModel(value = "用户修改模型")
public class UserEditDto {
    @ApiModelProperty(notes = "用户名")
    @NotEmpty
    @NotNull
    private String username;
    @ApiModelProperty(notes = "昵称")
    @NotEmpty
    @NotNull
    private String nick_name;
    @ApiModelProperty(notes = "邮箱")
    @NotEmpty
    @NotNull
    @Email
    private String email;
    @ApiModelProperty(notes = "电话")
    @NotEmpty
    @NotNull
    @Size(min = 11, max = 11)
    private String phone;

    private Set<SysRole> roles = new HashSet<>();

    private boolean enabled;
    @Min(0)
    private Long org_id;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNick_name() {
        return nick_name;
    }

    public void setNick_name(String nick_name) {
        this.nick_name = nick_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Long getOrg_id() {
        return org_id;
    }

    public void setOrg_id(Long org_id) {
        this.org_id = org_id;
    }

    public Set<SysRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<SysRole> roles) {
        this.roles = roles;
    }
}
