package csnight.redis.monitor.rest.task;

import csnight.redis.monitor.aop.LogAsync;
import csnight.redis.monitor.busi.task.MonitorRuleManagerImpl;
import csnight.redis.monitor.rest.task.dto.MonitorRuleDto;
import csnight.redis.monitor.utils.RespTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("rules")
@Api(tags = "监控规则管理API")
public class RmsMonitorRuleController {
    @Resource
    private MonitorRuleManagerImpl ruleManager;

    @LogAsync(module = "RULES", auth = "RULES_QUERY_ALL")
    @ApiOperation("获取所有监控规则")
    @PreAuthorize("hasAuthority('RULES_QUERY_ALL')")
    @RequestMapping(value = "/allMonitorRule", method = RequestMethod.GET)
    public RespTemplate GetAllRules() {
        return new RespTemplate(HttpStatus.OK, ruleManager.GetAllMonitorRule());
    }

    @LogAsync(module = "RULES", auth = "RULES_QUERY")
    @ApiOperation("获取当前用户所有监控规则")
    @PreAuthorize("hasAuthority('RULES_QUERY')")
    @RequestMapping(value = "/userMonitorRule", method = RequestMethod.GET)
    public RespTemplate GetRulesByUser() {
        return new RespTemplate(HttpStatus.OK, ruleManager.GetUserRules());
    }

    @LogAsync(module = "RULES", auth = "RULES_ADD")
    @ApiOperation("新增监控规则")
    @PreAuthorize("hasAuthority('RULES_ADD')")
    @RequestMapping(value = "/monitorRule", method = RequestMethod.POST)
    public RespTemplate AddMonitorRule(@Valid @RequestBody MonitorRuleDto dto) {
        return new RespTemplate(HttpStatus.OK, ruleManager.AddMonitorRule(dto));
    }

    @LogAsync(module = "RULES", auth = "RULES_UPDATE")
    @ApiOperation("修改监控规则状态")
    @PreAuthorize("hasAuthority('RULES_UPDATE')")
    @RequestMapping(value = "/monitorRule/{id}/{state}", method = RequestMethod.PUT)
    public RespTemplate UpdateRule(@PathVariable String id, @PathVariable boolean state) {
        return new RespTemplate(HttpStatus.OK, ruleManager.ChangeRuleState(id, state));
    }

    @LogAsync(module = "RULES", auth = "RULES_DEL")
    @ApiOperation("根据ID删除监控规则")
    @PreAuthorize("hasAuthority('RULES_DEL')")
    @RequestMapping(value = "/monitorRule/{id}", method = RequestMethod.DELETE)
    public RespTemplate GetCeJobByUser(@PathVariable String id) {
        return new RespTemplate(HttpStatus.OK, ruleManager.DeleteMonitorRule(id));
    }
}
