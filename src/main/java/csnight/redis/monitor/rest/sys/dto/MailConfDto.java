package csnight.redis.monitor.rest.sys.dto;

import io.swagger.annotations.ApiModel;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
@ApiModel(value = "邮件配置模型")
public class MailConfDto {
    @NotEmpty
    @NotNull
    private String host;
    @Max(65535)
    @Min(1)
    private int port = 465;
    @NotEmpty
    @NotNull
    private String email;
    @NotEmpty
    @NotNull
    private String pwd;
    @NotEmpty
    @NotNull
    private String username;
    private String encode = "UTF-8";

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
}
