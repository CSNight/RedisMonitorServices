package com.csnight.redis.monitor.rest.sys;

import com.csnight.redis.monitor.aop.LogAsync;
import com.csnight.redis.monitor.busi.sys.MenuServiceImpl;
import com.csnight.redis.monitor.busi.sys.exp.MenuQueryExp;
import com.csnight.redis.monitor.exception.ConflictsException;
import com.csnight.redis.monitor.rest.sys.dto.MenuDto;
import com.csnight.redis.monitor.utils.RespTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping(value = "menus")
@Api(tags = "菜单管理API")
public class MenuController {
    @Resource
    private MenuServiceImpl menuService;

    @LogAsync

    @PreAuthorize("hasAuthority('MENU_ICONS')")
    @ApiOperation(value = "获取系统图标库")
    @RequestMapping(value = "/get_icons", method = RequestMethod.GET)
    public RespTemplate GetIcons() {
        return new RespTemplate(HttpStatus.OK, menuService.GetIconList());
    }

    @LogAsync
    @PreAuthorize("hasAuthority('MENU_QUERY')")
    @ApiOperation(value = "获取菜单目录树")
    @RequestMapping(value = "/get_menu_tree", method = RequestMethod.GET)
    public RespTemplate GetMenuTree() {
        return new RespTemplate(HttpStatus.OK, menuService.GetMenuTree());
    }

    @LogAsync
    @PreAuthorize("hasAuthority('MENU_QUERY')")
    @ApiOperation(value = "获取菜单目录列表")
    @RequestMapping(value = "/get_menu_list", method = RequestMethod.GET)
    public RespTemplate GetMenuList() {
        return new RespTemplate(HttpStatus.OK, menuService.GetMenuList());
    }

    @LogAsync
    @PreAuthorize("hasAuthority('MENU_QUERY')")
    @ApiOperation(value = "通过父节点ID获取菜单目录")
    @RequestMapping(value = "/{pid}/get_menu", method = RequestMethod.GET)
    public RespTemplate GetMenuByPid(@PathVariable String pid) {
        return new RespTemplate(HttpStatus.OK, menuService.GetMenuByPid(pid));
    }

    @LogAsync
    @PreAuthorize("hasAuthority('MENU_QUERY')")
    @ApiOperation(value = "查询菜单目录")
    @RequestMapping(value = "/query_menu", method = RequestMethod.GET)
    public RespTemplate MenuQuery(MenuQueryExp exp) {
        return new RespTemplate(HttpStatus.OK, menuService.QueryBy(exp));
    }

    @LogAsync
    @PreAuthorize("hasAuthority('MENU_ROUTE')")
    @ApiOperation(value = "查询菜单路由")
    @RequestMapping(value = "/menu_routers", method = RequestMethod.GET)
    public RespTemplate MenuRoutes() {
        return new RespTemplate(HttpStatus.OK, menuService.GetMenuRouterByRole());
    }

    @LogAsync
    @ApiOperation(value = "修改菜单信息")
    @PreAuthorize("hasAuthority('MENU_UPDATE')")
    @RequestMapping(value = "/modify_menu", method = RequestMethod.PUT)
    public RespTemplate ModifyMenuIns(@Valid @RequestBody MenuDto menuDto) throws ConflictsException {
        if (menuDto != null) {
            return new RespTemplate(HttpStatus.OK, menuService.ModifyMenu(menuDto));
        }
        return new RespTemplate(HttpStatus.BAD_REQUEST, "");
    }

    @LogAsync
    @ApiOperation(value = "添加菜单项")
    @PreAuthorize("hasAuthority('MENU_ADD')")
    @RequestMapping(value = "/new_menu", method = RequestMethod.POST)
    public RespTemplate NewMenuIns(@Valid @RequestBody MenuDto menuDto) throws ConflictsException {
        if (menuDto != null) {
            return new RespTemplate(HttpStatus.OK, menuService.NewMenu(menuDto));
        }
        return new RespTemplate(HttpStatus.BAD_REQUEST, "");
    }

    @LogAsync
    @ApiOperation(value = "通过ID删除菜单")
    @PreAuthorize("hasAuthority('MENU_DEL')")
    @RequestMapping(value = "/delete_menu/{id}", method = RequestMethod.DELETE)
    public RespTemplate DeleteMenuById(@PathVariable String id) {
        return new RespTemplate(HttpStatus.OK, menuService.DeleteMenuById(id));
    }

}