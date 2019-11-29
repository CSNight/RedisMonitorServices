package csnight.redis.monitor.rest.rms;

import csnight.redis.monitor.aop.LogAsync;
import csnight.redis.monitor.busi.rms.RmsInsManageImpl;
import csnight.redis.monitor.exception.ConfigException;
import csnight.redis.monitor.rest.rms.dto.RmsInsDto;
import csnight.redis.monitor.utils.RespTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("instance")
@Api(tags = "Redis实例API")
public class RmsInsController {

    @Resource
    private RmsInsManageImpl rmsInsManage;

    @LogAsync(module = "INSTANCE", auth = "INS_QUERY_ALL")
    @ApiOperation("查询Redis实例列表")
    @RequestMapping(value = "/get_instances", method = RequestMethod.GET)
    public RespTemplate GetAllInstances() {
        return new RespTemplate(HttpStatus.OK, rmsInsManage.GetInstances());
    }

    @LogAsync(module = "INSTANCE", auth = "INS_QUERY")
    @ApiOperation("根据用户id查询Redis实例")
    @RequestMapping(value = "/get_instances/{user_id}", method = RequestMethod.GET)
    public RespTemplate GetInstanceByUser(@PathVariable String user_id) {
        return new RespTemplate(HttpStatus.OK, rmsInsManage.GetInstanceByUser(user_id));
    }

    @LogAsync(module = "INSTANCE", auth = "INS_ADD")
    @ApiOperation("新建Redis实例")
    @RequestMapping(value = "/new_instance", method = RequestMethod.POST)
    public RespTemplate AddInstance(@Valid @RequestBody RmsInsDto dto) throws ConfigException {
        return new RespTemplate(HttpStatus.OK, rmsInsManage.NewInstance(dto));
    }

    @LogAsync(module = "INSTANCE", auth = "INS_ADD")
    @ApiOperation("删除Redis实例")
    @RequestMapping(value = "/delete_instance/{ins_id}", method = RequestMethod.DELETE)
    public RespTemplate DeleteInstance(@PathVariable String ins_id) {
        return new RespTemplate(HttpStatus.OK, rmsInsManage.DeleteInstance(ins_id));
    }
}
