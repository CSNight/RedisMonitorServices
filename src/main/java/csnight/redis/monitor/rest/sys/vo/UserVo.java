package csnight.redis.monitor.rest.sys.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

@ApiModel(value = "用户信息模型")
public class UserVo {
    @ApiModelProperty(notes = "用户名")
    private String username;
    @ApiModelProperty(notes = "昵称")
    private String nick_name;
    @ApiModelProperty(notes = "邮箱")
    private String email;
    @ApiModelProperty(notes = "电话")
    private String phone;
    @ApiModelProperty(notes = "登录次数", example = "0")
    private int login_times;
    @ApiModelProperty(notes = "上次登录时间")
    private Date last_login;
    @ApiModelProperty(notes = "是否启用", example = "true")
    private boolean enabled;
    @ApiModelProperty(notes = "关联组织机构ID", example = "0")
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
