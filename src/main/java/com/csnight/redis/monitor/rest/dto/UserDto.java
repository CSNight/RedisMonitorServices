package com.csnight.redis.monitor.rest.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class UserDto {

    @NotEmpty
    @NotNull
    private String username;

    @NotEmpty
    @NotNull
    @Email
    private String email;

    @NotEmpty
    @NotNull
    @Size(min = 11, max = 11)
    private String phone;

    @NotEmpty
    @NotNull
    @Size(min = 6, max = 20)
    private String password;

    @NotEmpty
    @NotNull
    @Size(min = 6, max = 20)
    private String match_password;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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