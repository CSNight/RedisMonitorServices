package com.csnight.redis.monitor.rest;

import com.csnight.redis.monitor.aop.LogBack;
import com.csnight.redis.monitor.busi.RoleServiceImpl;
import com.csnight.redis.monitor.db.jpa.SysRole;
import com.csnight.redis.monitor.exception.ConflictsException;
import com.csnight.redis.monitor.rest.dto.RoleDto;
import com.csnight.redis.monitor.utils.RespTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = "roles")
@Api(tags = "角色管理API")
public class RoleController {
    @Resource
    private RoleServiceImpl roleService;

    @LogBack
    @ApiOperation(value = "获取角色列表")
    @RequestMapping(value = "/get_roles", method = RequestMethod.GET)
    public RespTemplate GetRoleList() {
        List<SysRole> roles = roleService.GetAllRole();
        if (roles != null) {
            return new RespTemplate(HttpStatus.OK, roles);
        } else {
            return new RespTemplate(HttpStatus.NOT_FOUND, "Can't find any records!");
        }
    }

    @LogBack
    @ApiOperation(value = "新建角色")
    @RequestMapping(value = "/new_role", method = RequestMethod.POST)
    public RespTemplate NewRole(@Valid @RequestBody RoleDto dto) throws ConflictsException {
        return new RespTemplate(HttpStatus.OK, roleService.NewRole(dto));
    }

    @LogBack
    @ApiOperation(value = "编辑角色")
    @RequestMapping(value = "/modify_role", method = RequestMethod.PUT)
    public RespTemplate ModifyRole(@Valid @RequestBody RoleDto dto) throws ConflictsException {
        return new RespTemplate(HttpStatus.OK, roleService.ModifyRole(dto));
    }

    @LogBack
    @ApiOperation(value = "编辑角色菜单")
    @RequestMapping(value = "/update_role_menus", method = RequestMethod.PUT)
    public RespTemplate ModifyRoleMenus(@Valid @RequestBody RoleDto dto) {
        return new RespTemplate(HttpStatus.OK, roleService.UpdateRoleMenus(dto));
    }

    @LogBack
    @ApiOperation(value = "编辑角色权限")
    @RequestMapping(value = "/update_role_permits", method = RequestMethod.PUT)
    public RespTemplate ModifyRolePermits(@Valid @RequestBody RoleDto dto) {
        return new RespTemplate(HttpStatus.OK, roleService.UpdateRolePermissions(dto));
    }

    @LogBack
    @ApiOperation(value = "删除角色")
    @RequestMapping(value = "/delete_role/{id}", method = RequestMethod.DELETE)
    public RespTemplate ModifyRole(@PathVariable String id) {
        return new RespTemplate(HttpStatus.OK, roleService.DeleteRoleById(id));
    }
}
