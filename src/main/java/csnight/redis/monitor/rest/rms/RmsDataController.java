package csnight.redis.monitor.rest.rms;

import csnight.redis.monitor.aop.LogAsync;
import csnight.redis.monitor.busi.rms.RmsDtManageImpl;
import csnight.redis.monitor.exception.ConfigException;
import csnight.redis.monitor.utils.RespTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author csnight
 * @description
 * @since 2019/12/31 15:33
 */
@RestController
@RequestMapping("dba")
@Api(tags = "Redis数据管理API")
public class RmsDataController {
    @Resource
    private RmsDtManageImpl dtManage;

    @LogAsync(module = "DBA", auth = "DBA_QUERY_ALL")
    @ApiOperation("查询Redis实例库列表")
    @PreAuthorize("hasAuthority('DBA_QUERY_ALL')")
    @RequestMapping(value = "/databases/all", method = RequestMethod.GET)
    public RespTemplate GetAllInstances() throws ConfigException {
        return new RespTemplate(HttpStatus.OK, dtManage.GetDatabases());
    }

    @LogAsync(module = "DBA", auth = "DBA_QUERY")
    @ApiOperation("根据用户id查询Redis实例库")
    @PreAuthorize("hasAuthority('DBA_QUERY')")
    @RequestMapping(value = "/databases/{user_id}", method = RequestMethod.GET)
    public RespTemplate GetInstanceByUser(@PathVariable String user_id) throws ConfigException {
        return new RespTemplate(HttpStatus.OK, dtManage.GetDatabaseByUser(user_id));
    }

    @LogAsync(module = "DBA", auth = "DBA_QUERY_SINGLE")
    @ApiOperation("根据实例id查询Redis实例库")
    @PreAuthorize("hasAuthority('DBA_QUERY')")
    @RequestMapping(value = "/database/{ins_id}", method = RequestMethod.GET)
    public RespTemplate GetInstanceById(@PathVariable String ins_id) throws ConfigException {
        return new RespTemplate(HttpStatus.OK, dtManage.GetDatabaseById(ins_id));
    }
}
