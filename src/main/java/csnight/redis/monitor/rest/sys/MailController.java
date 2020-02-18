package csnight.redis.monitor.rest.sys;

import csnight.redis.monitor.aop.LogAsync;
import csnight.redis.monitor.busi.sys.MailSendService;
import csnight.redis.monitor.rest.sys.dto.MailConfDto;
import csnight.redis.monitor.utils.RespTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping(value = "mail")
@Api(tags = "邮件管理API")
public class MailController {
    @Resource
    private MailSendService mailService;

    @LogAsync(module = "MAIL", auth = "MAIL_CONF_ALL")
    @PreAuthorize("hasAuthority('MAIL_CONF_ALL')")
    @ApiOperation(value = "查询全部邮件配置")
    @RequestMapping(value = "/getAllConf", method = RequestMethod.GET)
    public RespTemplate GetAllMailConf() {
        return new RespTemplate(HttpStatus.OK, mailService.GetAllMailConfig());
    }

    @LogAsync(module = "MAIL", auth = "MAIL_CONF_QUERY")
    @PreAuthorize("hasAuthority('MAIL_CONF_QUERY')")
    @ApiOperation(value = "查询用户邮件配置")
    @RequestMapping(value = "/getUserConf", method = RequestMethod.GET)
    public RespTemplate GetMailConf() {
        return new RespTemplate(HttpStatus.OK, mailService.GetUserMailConfig());
    }

    @LogAsync(module = "MAIL", auth = "MAIL_CONF_UPDATE")
    @PreAuthorize("hasAuthority('MAIL_CONF_UPDATE')")
    @ApiOperation(value = "用户邮件配置")
    @RequestMapping(value = "/editUserConf", method = RequestMethod.PUT)
    public RespTemplate UpdateMailConf(@Valid @RequestBody MailConfDto dto) {
        return new RespTemplate(HttpStatus.OK, mailService.UpdateMailConfig(dto));
    }

    @LogAsync(module = "MAIL", auth = "MAIL_CONF_DEL")
    @PreAuthorize("hasAuthority('MAIL_CONF_DEL')")
    @ApiOperation(value = "用户邮件配置")
    @RequestMapping(value = "/deleteConf", method = RequestMethod.DELETE)
    public RespTemplate DeleteMailConf() {
        return new RespTemplate(HttpStatus.OK, mailService.DeleteMailConfig());
    }
}
