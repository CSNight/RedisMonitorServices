package csnight.redis.monitor.rest.sys.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.*;
import java.util.HashSet;
import java.util.Set;

@ApiModel(value = "邮件配置模型")
public class MailConfDto {
    @ApiModelProperty(notes = "SMTP地址", example = "smtp.*.com")
    @NotEmpty
    @NotNull
    private String host;
    @ApiModelProperty(notes = "SMTP地址", example = "0")
    @Max(65535)
    @Min(1)
    private int port = 465;
    @NotEmpty
    @NotNull
    @Email
    @ApiModelProperty(notes = "邮箱账号", example = "abc@qq.com")
    private String email;
    @NotEmpty
    @NotNull
    @ApiModelProperty(notes = "密码/授权码", example = "123456")
    private String pwd;
    @ApiModelProperty(notes = "用户名", example = "abc")
    private String username;
    @ApiModelProperty(notes = "字符集", example = "UTF-8")
    private String encode = "UTF-8";
    @ApiModelProperty(notes = "常用收件人")
    private Set<String> receivers = new HashSet<>();

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEncode() {
        return encode;
    }

    public void setEncode(String encode) {
        this.encode = encode;
    }

    public Set<String> getReceivers() {
        return receivers;
    }

    public void setReceivers(Set<String> receivers) {
        this.receivers = receivers;
    }
}
