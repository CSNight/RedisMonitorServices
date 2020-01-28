package csnight.redis.monitor.rest.rms;

import csnight.redis.monitor.aop.LogAsync;
import csnight.redis.monitor.busi.rms.RmsInsManageImpl;
import csnight.redis.monitor.busi.rms.exp.InsQueryExp;
import csnight.redis.monitor.exception.ConfigException;
import csnight.redis.monitor.exception.ConflictsException;
import csnight.redis.monitor.rest.rms.dto.RmsInsDto;
import csnight.redis.monitor.utils.RespTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("instance")
@Api(tags = "Redis实例管理API")
public class RmsInsController {

    @Resource
    private RmsInsManageImpl rmsInsManage;

    @LogAsync(module = "INSTANCE", auth = "INS_QUERY_ALL")
    @ApiOperation("查询Redis实例列表")
    @PreAuthorize("hasAuthority('INS_QUERY_ALL')")
    @RequestMapping(value = "/get_instances/{update}", method = RequestMethod.GET)
    public RespTemplate GetAllInstances(@PathVariable String update) {
        return new RespTemplate(HttpStatus.OK, rmsInsManage.GetInstances(update));
    }

    @LogAsync(module = "INSTANCE", auth = "INS_QUERY")
    @ApiOperation("根据用户id查询Redis实例")
    @PreAuthorize("hasAuthority('INS_QUERY')")
    @RequestMapping(value = "/get_instance/{user_id}", method = RequestMethod.GET)
    public RespTemplate GetInstanceByUser(@PathVariable String user_id) {
        return new RespTemplate(HttpStatus.OK, rmsInsManage.GetInstanceByUser(user_id));
    }

    @LogAsync(module = "INSTANCE", auth = "INS_SEARCH")
    @ApiOperation("搜索实例")
    @PreAuthorize("hasAuthority('INS_SEARCH')")
    @RequestMapping(value = "/query_instance", method = RequestMethod.GET)
    public RespTemplate SearchInstances(InsQueryExp exp) {
        return new RespTemplate(HttpStatus.OK, rmsInsManage.QueryBy(exp));
    }

    @LogAsync(module = "INSTANCE", auth = "INS_ADD")
    @ApiOperation("新建Redis实例")
    @PreAuthorize("hasAuthority('INS_ADD')")
    @RequestMapping(value = "/new_instance", method = RequestMethod.POST)
    public RespTemplate AddInstance(@Valid @RequestBody RmsInsDto dto) throws ConfigException, ConflictsException {
        return new RespTemplate(HttpStatus.OK, rmsInsManage.NewInstance(dto));
    }

    @LogAsync(module = "INSTANCE", auth = "INS_UPDATE_INFO")
    @ApiOperation("修改实例名称")
    @PreAuthorize("hasAuthority('INS_UPDATE_INFO')")
    @RequestMapping(value = "/modify_name", method = RequestMethod.PUT)
    public RespTemplate ModifyInsName(@Valid @RequestBody RmsInsDto dto) throws ConflictsException {
        return new RespTemplate(HttpStatus.OK, rmsInsManage.ModifyInsName(dto));
    }

    @LogAsync(module = "INSTANCE", auth = "INS_UPDATE_CONN")
    @ApiOperation("修改实例连接信息")
    @PreAuthorize("hasAuthority('INS_UPDATE_CONN')")
    @RequestMapping(value = "/modify_conn", method = RequestMethod.PUT)
    public RespTemplate ModifyInsConn(@Valid @RequestBody RmsInsDto dto) throws ConfigException {
        return new RespTemplate(HttpStatus.OK, rmsInsManage.ModifyInsConn(dto));
    }

    @LogAsync(module = "INSTANCE", auth = "INS_UPDATE_STATE")
    @ApiOperation("修改实例连接状态")
    @PreAuthorize("hasAuthority('INS_UPDATE_STATE')")
    @RequestMapping(value = "/modify_state", method = RequestMethod.PUT)
    public RespTemplate ModifyInsState(@Valid @RequestBody RmsInsDto dto) throws ConfigException {
        return new RespTemplate(HttpStatus.OK, rmsInsManage.ModifyInsState(dto));
    }

    @LogAsync(module = "INSTANCE", auth = "INS_REFRESH_META")
    @ApiOperation("更新实例信息")
    @PreAuthorize("hasAuthority('INS_REFRESH_META')")
    @RequestMapping(value = "/update_meta/{ins_id}", method = RequestMethod.PUT)
    public RespTemplate ModifyInsInfo(@PathVariable String ins_id) throws ConfigException {
        return new RespTemplate(HttpStatus.OK, rmsInsManage.UpdateInsMeta(ins_id));
    }

    @LogAsync(module = "INSTANCE", auth = "INS_DEL")
    @ApiOperation("删除Redis实例")
    @PreAuthorize("hasAuthority('INS_DEL')")
    @RequestMapping(value = "/delete_instance/{ins_id}", method = RequestMethod.DELETE)
    public RespTemplate DeleteInstance(@PathVariable String ins_id) {
        return new RespTemplate(HttpStatus.OK, rmsInsManage.DeleteInstance(ins_id));
    }
}
