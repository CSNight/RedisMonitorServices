package com.csnight.redis.monitor.rest;

import com.csnight.redis.monitor.aop.LogBack;
import com.csnight.redis.monitor.busi.PermissionServiceImpl;
import com.csnight.redis.monitor.busi.exp.PermitQueryExp;
import com.csnight.redis.monitor.exception.ConflictsException;
import com.csnight.redis.monitor.rest.dto.PermissionDto;
import com.csnight.redis.monitor.utils.BaseUtils;
import com.csnight.redis.monitor.utils.RespTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping(value = "permit")
@Api(tags = "权限管理API")
public class PermitController {

    @Resource
    private PermissionServiceImpl permissionService;

    @LogBack
    @ApiOperation(value = "查询权限")
    @RequestMapping(value = "/query_permit", method = RequestMethod.GET)
    public RespTemplate PermitQuery(PermitQueryExp exp) {
        return new RespTemplate(HttpStatus.OK, permissionService.QueryBy(exp));
    }

    @LogBack
    @ApiOperation(value = "获取权限列表")
    @RequestMapping(value = "/get_permits", method = RequestMethod.GET)
    public RespTemplate GetPermissionList() {
        return new RespTemplate(HttpStatus.OK, permissionService.GetAllPermission());
    }

    @LogBack
    @ApiOperation(value = "创建权限")
    @RequestMapping(value = "/create_permit", method = RequestMethod.POST)
    public RespTemplate CreatePermit(@Valid @RequestBody PermissionDto dto) throws ConflictsException {
        String username = BaseUtils.GetUserFromContext();
        return new RespTemplate(HttpStatus.OK, permissionService.NewPermission(dto, username));
    }

    @LogBack
    @ApiOperation(value = "删除权限")
    @RequestMapping(value = "/delete_permit/{id}", method = RequestMethod.DELETE)
    public RespTemplate DeletePermit(@PathVariable String id) {
        return new RespTemplate(HttpStatus.OK, permissionService.DeletePermitById(id));
    }

    @LogBack
    @ApiOperation(value = "修改权限")
    @RequestMapping(value = "/edit_permit", method = RequestMethod.PUT)
    public RespTemplate ModifyPermit(@Valid @RequestBody PermissionDto dto) throws ConflictsException {
        return new RespTemplate(HttpStatus.OK, permissionService.ModifyPermission(dto));
    }
}
