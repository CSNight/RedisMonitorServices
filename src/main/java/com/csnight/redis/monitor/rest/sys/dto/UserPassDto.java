package com.csnight.redis.monitor.rest.sys.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@ApiModel(value = "密码变更模型")
public class UserPassDto {
    @ApiModelProperty(notes = "用户名")
    @NotEmpty
    @NotNull
    private String username;
    @ApiModelProperty(notes = "密码")
    @NotEmpty
    @NotNull
    @Size(min = 6, max = 50)
    private String old_password;
    @ApiModelProperty(notes = "密码")
    @NotEmpty
    @NotNull
    @Size(min = 6, max = 50)
    private String password;
    @ApiModelProperty(notes = "密码核对")
    @NotEmpty
    @NotNull
    @Size(min = 6, max = 50)
    private String match_password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOld_password() {
        return old_password;
    }

    public void setOld_password(String old_password) {
        this.old_password = old_password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMatch_password() {
        return match_password;
    }

    public void setMatch_password(String match_password) {
        this.match_password = match_password;
    }
}
