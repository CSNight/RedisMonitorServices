package com.csnight.redis.monitor.rest;

import com.csnight.redis.monitor.aop.LogBack;
import com.csnight.redis.monitor.busi.RoleServiceImpl;
import com.csnight.redis.monitor.utils.RespTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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
        return new RespTemplate(HttpStatus.OK, roleService.GetAllRole());
    }
}
