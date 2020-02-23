package csnight.redis.monitor.rest.task;

import csnight.redis.monitor.aop.LogAsync;
import csnight.redis.monitor.busi.task.CETaskManagerImpl;
import csnight.redis.monitor.rest.task.dto.TaskConfDto;
import csnight.redis.monitor.utils.RespTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("execution")
@Api(tags = "命令任务管理API")
public class RmsCmdTaskController {
    @Resource
    private CETaskManagerImpl ceTaskManager;

    @LogAsync(module = "CETASK", auth = "CETASK_QUERY_ALL")
    @ApiOperation("获取所有命令任务")
    @PreAuthorize("hasAuthority('CETASK_QUERY_ALL')")
    @RequestMapping(value = "/cetAll", method = RequestMethod.GET)
    public RespTemplate GetAllCeJob() {
        return new RespTemplate(HttpStatus.OK, ceTaskManager.GetAllJob());
    }

    @LogAsync(module = "CETASK", auth = "CETTASK_QUERY")
    @ApiOperation("获取当前用户所有命令任务")
    @PreAuthorize("hasAuthority('CETTASK_QUERY')")
    @RequestMapping(value = "/cetByUser", method = RequestMethod.GET)
    public RespTemplate GetCeJobByUser() {
        return new RespTemplate(HttpStatus.OK, ceTaskManager.GetUserJob());
    }

    @LogAsync(module = "CETASK", auth = "CETASK_QUERY_ID")
    @ApiOperation("根据ID获取命令任务")
    @PreAuthorize("hasAuthority('CETASK_QUERY_ID')")
    @RequestMapping(value = "/cetById/{id}", method = RequestMethod.GET)
    public RespTemplate GetCeJobById(@PathVariable String id) {
        return new RespTemplate(HttpStatus.OK, ceTaskManager.GetJobById(id));
    }

    @LogAsync(module = "CETASK", auth = "CETASK_ADD")
    @ApiOperation("新增命令任务")
    @PreAuthorize("hasAuthority('CETASK_ADD')")
    @RequestMapping(value = "/cetAdd", method = RequestMethod.POST)
    public RespTemplate AddNewRedisCeJob(@Valid @RequestBody TaskConfDto dto) {
        return new RespTemplate(HttpStatus.OK, ceTaskManager.addCmdExeJob(dto));
    }
}
