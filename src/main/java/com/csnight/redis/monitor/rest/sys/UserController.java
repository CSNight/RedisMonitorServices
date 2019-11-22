package com.csnight.redis.monitor.rest.sys;

import com.csnight.redis.monitor.aop.LogAsync;
import com.csnight.redis.monitor.busi.sys.UserServiceImpl;
import com.csnight.redis.monitor.busi.sys.exp.UserQueryExp;
import com.csnight.redis.monitor.exception.ConflictsException;
import com.csnight.redis.monitor.rest.sys.dto.UserEditDto;
import com.csnight.redis.monitor.rest.sys.dto.UserPassDto;
import com.csnight.redis.monitor.utils.BaseUtils;
import com.csnight.redis.monitor.utils.RespTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping(value = "users")
@Api(tags = "用户管理API")
public class UserController {
    @Resource
    private UserServiceImpl userService;

    @LogAsync
    @PreAuthorize("hasAuthority('USER_QUERY')")
    @ApiOperation(value = "用户目录")
    @RequestMapping(value = "/get_users", method = RequestMethod.GET)
    public RespTemplate GetAllUsers() {
        return new RespTemplate(HttpStatus.OK, userService.GetAllUser());
    }

    @LogAsync
    @PreAuthorize("hasAuthority('USER_QUERY')")
    @ApiOperation(value = "用户搜索")
    @RequestMapping(value = "/query_users", method = RequestMethod.GET)
    public RespTemplate QueryUsers(UserQueryExp exp) {
        return new RespTemplate(HttpStatus.OK, userService.QueryBy(exp));
    }

    @LogAsync
    @PreAuthorize("hasAuthority('USER_QUERY') AND hasAuthority('ORG_QUERY')")
    @ApiOperation(value = "根据组织查询用户")
    @RequestMapping(value = "/get_by_org/{org_id}", method = RequestMethod.GET)
    public RespTemplate GetUserByOrg(@PathVariable String org_id) {
        return new RespTemplate(HttpStatus.OK, userService.GetUsersByOrg(Long.parseLong(org_id)));
    }

    @LogAsync
    @PreAuthorize("hasAuthority('USER_ADD')")
    @ApiOperation(value = "新增用户")
    @RequestMapping(value = "/new_user", method = RequestMethod.POST)
    public RespTemplate NewUser(@Valid @RequestBody UserEditDto dto) throws ConflictsException {
        return new RespTemplate(HttpStatus.OK, userService.NewUsr(dto));
    }

    @LogAsync
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    @ApiOperation(value = "修改用户信息")
    @RequestMapping(value = "/edit_user", method = RequestMethod.PUT)
    public RespTemplate ModifyUserInfo(@Valid @RequestBody UserEditDto dto) throws ConflictsException {
        return new RespTemplate(HttpStatus.OK, userService.ModifyUser(dto));
    }

    @LogAsync
    @PreAuthorize("hasAuthority('USER_INFO_EDIT')")
    @ApiOperation(value = "修改用户密码")
    @RequestMapping(value = "/change_pwd", method = RequestMethod.PUT)
    public RespTemplate ModifyUserPass(@Valid @RequestBody UserPassDto dto) {
        return new RespTemplate(HttpStatus.OK, userService.ChangePassword(dto));
    }

    @LogAsync
    @PreAuthorize("hasAuthority('USER_INFO_EDIT')")
    @ApiOperation(value = "修改用户头像")
    @RequestMapping(value = "/change_avatar", method = RequestMethod.POST)
    public RespTemplate ChangeUserAvatar(MultipartFile file) {
        return new RespTemplate(HttpStatus.OK, userService.changeAvatar(file, BaseUtils.GetUserFromContext()));
    }

    @LogAsync
    @PreAuthorize("hasAuthority('USER_DEL')")
    @ApiOperation(value = "通过ID删除用户")
    @RequestMapping(value = "/delete_by_id/{id}", method = RequestMethod.DELETE)
    public RespTemplate DeleteUserById(@PathVariable String id) {
        return new RespTemplate(HttpStatus.OK, userService.DeleteUserById(id));
    }

    @LogAsync
    @PreAuthorize("hasAuthority('USER_DEL')")
    @ApiOperation(value = "通过用户名删除用户")
    @RequestMapping(value = "/delete_by_name/{name}", method = RequestMethod.DELETE)
    public RespTemplate DeleteUserByName(@PathVariable String name) {
        return new RespTemplate(HttpStatus.OK, userService.DeleteUserByName(name));
    }
}
