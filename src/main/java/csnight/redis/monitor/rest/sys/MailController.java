package csnight.redis.monitor.rest.sys;

import csnight.redis.monitor.aop.LogAsync;
import csnight.redis.monitor.busi.sys.MailSendServiceImpl;
import csnight.redis.monitor.busi.sys.exp.MailRecQueryExp;
import csnight.redis.monitor.rest.sys.dto.MailConfDto;
import csnight.redis.monitor.rest.sys.dto.MailDelDto;
import csnight.redis.monitor.rest.sys.dto.MailSendDto;
import csnight.redis.monitor.utils.RespTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping(value = "mail")
@Api(tags = "邮件管理API")
public class MailController {
    @Resource
    private MailSendServiceImpl mailService;

    @LogAsync(module = "MAIL", auth = "MAIL_CONF_ALL")
    @PreAuthorize("hasAuthority('MAIL_CONF_ALL')")
    @ApiOperation(value = "查询全部邮件配置")
    @RequestMapping(value = "/getAllConf", method = RequestMethod.GET)
    public RespTemplate GetAllMailConf() {
        return new RespTemplate(HttpStatus.OK, mailService.GetAllMailConfig());
    }

    @LogAsync(module = "MAIL", auth = "MAIL_REC_ALL")
    @PreAuthorize("hasAuthority('MAIL_REC_ALL')")
    @ApiOperation(value = "查询全部邮件记录")
    @RequestMapping(value = "/getAllRec", method = RequestMethod.GET)
    public RespTemplate GetAllMailRec() {
        return new RespTemplate(HttpStatus.OK, mailService.GetAllMailRecords());
    }

    @LogAsync(module = "MAIL", auth = "MAIL_CONF_QUERY")
    @PreAuthorize("hasAuthority('MAIL_CONF_QUERY')")
    @ApiOperation(value = "查询用户邮件配置")
    @RequestMapping(value = "/getUserConf", method = RequestMethod.GET)
    public RespTemplate GetUserMailConf() {
        return new RespTemplate(HttpStatus.OK, mailService.GetUserMailConfig());
    }

    @LogAsync(module = "MAIL", auth = "MAIL_REC_QUERY")
    @PreAuthorize("hasAuthority('MAIL_REC_QUERY')")
    @ApiOperation(value = "查询用户邮件记录")
    @RequestMapping(value = "/getUserRec", method = RequestMethod.GET)
    public RespTemplate GetUserMailRec() {
        return new RespTemplate(HttpStatus.OK, mailService.GetUserMailRecord());
    }

    @LogAsync(module = "MAIL", auth = "MAIL_REC_ID")
    @PreAuthorize("hasAuthority('MAIL_REC_ID')")
    @ApiOperation(value = "根据ID查询用户邮件记录")
    @RequestMapping(value = "/getRecByID/{id}", method = RequestMethod.GET)
    public RespTemplate GetMailRecByID(@PathVariable String id) {
        return new RespTemplate(HttpStatus.OK, mailService.GetUserMailRecordById(id));
    }

    @LogAsync(module = "MAIL", auth = "MAIL_REC_SEARCH")
    @PreAuthorize("hasAuthority('MAIL_REC_SEARCH')")
    @ApiOperation(value = "搜索用户邮件记录")
    @RequestMapping(value = "/searchUserRec", method = RequestMethod.GET)
    public RespTemplate SearchUserMailRec(MailRecQueryExp exp) {
        return new RespTemplate(HttpStatus.OK, mailService.QueryMailRecord(exp));
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
    @ApiOperation(value = "邮件配置删除")
    @RequestMapping(value = "/deleteConf", method = RequestMethod.DELETE)
    public RespTemplate DeleteMailConf() {
        return new RespTemplate(HttpStatus.OK, mailService.DeleteMailConfig());
    }

    @LogAsync(module = "MAIL", auth = "MAIL_REC_DEL")
    @PreAuthorize("hasAuthority('MAIL_REC_DEL')")
    @ApiOperation(value = "根据ID删除邮件记录")
    @RequestMapping(value = "/deleteRecById/{id}", method = RequestMethod.DELETE)
    public RespTemplate DeleteMailRecById(@PathVariable String id) {
        return new RespTemplate(HttpStatus.OK, mailService.DeleteRecordById(id));
    }

    @LogAsync(module = "MAIL", auth = "MAIL_REC_CLEAR")
    @PreAuthorize("hasAuthority('MAIL_REC_CLEAR')")
    @ApiOperation(value = "根据ID批量删除邮件记录")
    @RequestMapping(value = "/deleteRecMulti", method = RequestMethod.DELETE)
    public RespTemplate DeleteMailRecMulti(@RequestBody MailDelDto dto) {
        return new RespTemplate(HttpStatus.OK, mailService.DeleteRecordMulti(dto));
    }

    @LogAsync(module = "MAIL", auth = "MAIL_SEND")
    @PreAuthorize("hasAuthority('MAIL_SEND')")
    @ApiOperation(value = "发送邮件")
    @RequestMapping(value = "/sendMail", method = RequestMethod.POST)
    public RespTemplate SendMail(@Valid @RequestBody MailSendDto dto) {
        return new RespTemplate(HttpStatus.OK, mailService.SendMail(dto));
    }
}
