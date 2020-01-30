package csnight.redis.monitor.rest.task;

import csnight.redis.monitor.aop.LogAsync;
import csnight.redis.monitor.busi.task.TaskManagerImpl;
import csnight.redis.monitor.utils.RespTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@Api(tags = "任务管理API")
public class RmsTaskController {
    @Resource
    private TaskManagerImpl taskManager;

    @LogAsync(module = "TASK", auth = "TASK_QUERY_ALL")
    @ApiOperation("获取所有任务")
    //@PreAuthorize("hasAuthority('BACKUP_CLEAR')")
    @RequestMapping(value = "/getAll", method = RequestMethod.GET)
    public RespTemplate ClearRecords() {
        return new RespTemplate(HttpStatus.OK, taskManager.GetAllJob());
    }
}
