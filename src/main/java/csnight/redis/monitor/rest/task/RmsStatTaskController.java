package csnight.redis.monitor.rest.task;

import csnight.redis.monitor.aop.LogAsync;
import csnight.redis.monitor.busi.task.StatTaskManagerImpl;
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
@RequestMapping("statistic")
@Api(tags = "统计任务管理API")
public class RmsStatTaskController {
    @Resource
    private StatTaskManagerImpl statTaskManager;

    @LogAsync(module = "STTASK", auth = "STTASK_QUERY_ALL")
    @ApiOperation("获取所有统计任务")
    @PreAuthorize("hasAuthority('STTASK_QUERY_ALL')")
    @RequestMapping(value = "/statAll", method = RequestMethod.GET)
    public RespTemplate GetAllStatJob() {
        return new RespTemplate(HttpStatus.OK, statTaskManager.GetAllJob());
    }

    @LogAsync(module = "STTASK", auth = "STTASK_QUERY")
    @ApiOperation("获取当前用户所有统计任务")
    @PreAuthorize("hasAuthority('STTASK_QUERY')")
    @RequestMapping(value = "/statByUser", method = RequestMethod.GET)
    public RespTemplate GetStatJobByUser() {
        return new RespTemplate(HttpStatus.OK, statTaskManager.GetUserJob());
    }

    @LogAsync(module = "STTASK", auth = "STTASK_QUERY_ID")
    @ApiOperation("根据ID获取统计任务")
    @PreAuthorize("hasAuthority('STTASK_QUERY_ID')")
    @RequestMapping(value = "/statById/{id}", method = RequestMethod.GET)
    public RespTemplate GetStatJobById(@PathVariable String id) {
        return new RespTemplate(HttpStatus.OK, statTaskManager.GetJobById(id));
    }

    @LogAsync(module = "STTASK", auth = "STTASK_ADD")
    @ApiOperation("新增统计任务")
    @PreAuthorize("hasAuthority('STTASK_ADD')")
    @RequestMapping(value = "/statAdd", method = RequestMethod.POST)
    public RespTemplate AddNewRedisStatJob(@Valid @RequestBody TaskConfDto dto) {
        return new RespTemplate(HttpStatus.OK, statTaskManager.addRedisStatJob(dto));
    }

    @LogAsync(module = "STTASK", auth = "STTASK_CONF_UPDATE")
    @ApiOperation("更新统计任务设置")
    @PreAuthorize("hasAuthority('STTASK_CONF_UPDATE')")
    @RequestMapping(value = "/statConfUpdate", method = RequestMethod.PUT)
    public RespTemplate UpdateRedisStatJobConf(@Valid @RequestBody TaskConfDto dto) {
        return new RespTemplate(HttpStatus.OK, statTaskManager.ModifyRedisStatJobConf(dto));
    }

    @LogAsync(module = "STTASK", auth = "STTASK_STATE_UPDATE")
    @ApiOperation("更新统计任务状态")
    @PreAuthorize("hasAuthority('STTASK_STATE_UPDATE')")
    @RequestMapping(value = "/statStateUpdate/{ins_id}/{state}", method = RequestMethod.PUT)
    public RespTemplate UpdateRedisStatJobState(@PathVariable String ins_id, @PathVariable boolean state) {
        return new RespTemplate(HttpStatus.OK, statTaskManager.ModifyRedisStatJobState(ins_id, state));
    }

    @LogAsync(module = "STTASK", auth = "STTASK_DATA_UPDATE")
    @ApiOperation("更新统计任务数据")
    //@PreAuthorize("hasAuthority('STTASK_DATA_UPDATE')")
    @RequestMapping(value = "/statDataUpdate/{ins_id}", method = RequestMethod.PUT)
    public RespTemplate UpdateRedisStatJobState(@PathVariable String ins_id) {
        return new RespTemplate(HttpStatus.OK, statTaskManager.ModifyRedisJobData(ins_id, "ccccc", "ddddd"));
    }

    @LogAsync(module = "STTASK", auth = "STTASK_DEL")
    @ApiOperation("删除统计任务")
    @PreAuthorize("hasAuthority('STTASK_DEL')")
    @RequestMapping(value = "/statDelete/{ins_id}", method = RequestMethod.DELETE)
    public RespTemplate DeleteRedisStatJob(@PathVariable String ins_id) {
        return new RespTemplate(HttpStatus.OK, statTaskManager.DeleteRedisStatJob(ins_id));
    }
}
