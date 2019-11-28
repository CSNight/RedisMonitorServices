package csnight.redis.monitor.rest.sys;

import csnight.redis.monitor.aop.LogAsync;
import csnight.redis.monitor.busi.sys.PermissionServiceImpl;
import csnight.redis.monitor.busi.sys.exp.PermitQueryExp;
import csnight.redis.monitor.exception.ConflictsException;
import csnight.redis.monitor.rest.sys.dto.PermissionDto;
import csnight.redis.monitor.utils.BaseUtils;
import csnight.redis.monitor.utils.RespTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping(value = "permit")
@Api(tags = "权限管理API")
public class PermitController {

    @Resource
    private PermissionServiceImpl permissionService;

    @LogAsync(module = "PERMITS", op = "RIGHTS_QUERY")
    @PreAuthorize("hasAuthority('RIGHTS_QUERY')")
    @ApiOperation(value = "查询权限")
    @RequestMapping(value = "/query_permit", method = RequestMethod.GET)
    public RespTemplate PermitQuery(PermitQueryExp exp) {
        return new RespTemplate(HttpStatus.OK, permissionService.QueryBy(exp));
    }

    @LogAsync(module = "PERMITS", op = "RIGHTS_QUERY")
    @PreAuthorize("hasAuthority('RIGHTS_QUERY')")
    @ApiOperation(value = "获取权限列表")
    @RequestMapping(value = "/get_permits", method = RequestMethod.GET)
    public RespTemplate GetPermissionList() {
        return new RespTemplate(HttpStatus.OK, permissionService.GetAllPermission());
    }

    @LogAsync(module = "PERMITS", op = "RIGHTS_ADD")
    @PreAuthorize("hasAuthority('RIGHTS_ADD')")
    @ApiOperation(value = "创建权限")
    @RequestMapping(value = "/create_permit", method = RequestMethod.POST)
    public RespTemplate CreatePermit(@Valid @RequestBody PermissionDto dto) throws ConflictsException {
        String username = BaseUtils.GetUserFromContext();
        return new RespTemplate(HttpStatus.OK, permissionService.NewPermission(dto, username));
    }

    @LogAsync(module = "PERMITS", op = "RIGHTS_DEL")
    @PreAuthorize("hasAuthority('RIGHTS_DEL')")
    @ApiOperation(value = "删除权限")
    @RequestMapping(value = "/delete_permit/{id}", method = RequestMethod.DELETE)
    public RespTemplate DeletePermit(@PathVariable String id) {
        return new RespTemplate(HttpStatus.OK, permissionService.DeletePermitById(id));
    }

    @LogAsync(module = "PERMITS", op = "RIGHTS_UPDATE")
    @PreAuthorize("hasAuthority('RIGHTS_UPDATE')")
    @ApiOperation(value = "修改权限")
    @RequestMapping(value = "/edit_permit", method = RequestMethod.PUT)
    public RespTemplate ModifyPermit(@Valid @RequestBody PermissionDto dto) throws ConflictsException {
        return new RespTemplate(HttpStatus.OK, permissionService.ModifyPermission(dto));
    }
}
