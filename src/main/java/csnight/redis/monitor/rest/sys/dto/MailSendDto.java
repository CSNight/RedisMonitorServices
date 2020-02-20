package csnight.redis.monitor.rest.sys.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@ApiModel(value = "邮件发送模型")
public class MailSendDto {
    @ApiModelProperty(notes = "主题")
    @NotEmpty
    @NotNull
    private String subject;
    @ApiModelProperty(notes = "收件人")
    private Set<String> toList = new HashSet<>();
    @ApiModelProperty(notes = "抄送人")
    private Set<String> ccList = new HashSet<>();
    @ApiModelProperty(notes = "邮件内容")
    @NotNull
    private String content;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Set<String> getToList() {
        return toList;
    }

    public void setToList(Set<String> toList) {
        this.toList = toList;
    }

    public Set<String> getCcList() {
        return ccList;
    }

    public void setCcList(Set<String> ccList) {
        this.ccList = ccList;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
