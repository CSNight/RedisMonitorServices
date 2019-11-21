package com.csnight.redis.monitor.rest.sys.vo;

import io.swagger.annotations.ApiModel;

import java.util.Date;

@ApiModel(value = "用户信息模型")
public class UserVo {
    private String username;
    private String nick_name;
    private String email;
    private String phone;
    private int login_times;
    private Date last_login;
    private boolean enabled;
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

    public int getLogin_times() {
        return login_times;
    }

    public void setLogin_times(int login_times) {
        this.login_times = login_times;
    }

    public Date getLast_login() {
        return last_login;
    }

    public void setLast_login(Date last_login) {
        this.last_login = last_login;
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
}
