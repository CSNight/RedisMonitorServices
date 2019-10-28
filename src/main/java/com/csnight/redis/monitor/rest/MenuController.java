package com.csnight.redis.monitor.rest;

import com.csnight.redis.monitor.aop.LogBack;
import com.csnight.redis.monitor.busi.MenuServiceImpl;
import com.csnight.redis.monitor.rest.dto.MenuDto;
import com.csnight.redis.monitor.utils.RespTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "menus")
@Api(tags = "菜单API")
public class MenuController {
    private MenuServiceImpl menuService;

    public MenuController(MenuServiceImpl menuService) {
        this.menuService = menuService;
    }

    @LogBack
    @ApiOperation(value = "获取菜单目录树")
    @RequestMapping(value = "/get_menu_tree", method = RequestMethod.GET)
    public RespTemplate GetMenuTree() {
        return new RespTemplate(HttpStatus.OK, menuService.GetMenuTree());
    }

    @LogBack
    @ApiOperation(value = "获取菜单目录列表")
    @RequestMapping(value = "/get_menu_list", method = RequestMethod.GET)
    public RespTemplate GetMenuList() {
        return new RespTemplate(HttpStatus.OK, menuService.GetMenuList());
    }

    @LogBack
    @ApiOperation(value = "通过父节点ID获取菜单目录")
    @RequestMapping(value = "/{pid}/get_menu", method = RequestMethod.GET)
    public RespTemplate GetMenuByPid(@PathVariable String pid) {
        return new RespTemplate(HttpStatus.OK, menuService.GetMenuByPid(pid));
    }

    @LogBack
    @ApiOperation(value = "修改菜单信息")
    @RequestMapping(value = "/modify_menu", method = RequestMethod.PUT)
    public RespTemplate ModifyMenuIns(@Valid @RequestBody MenuDto menuDto) {
        if (menuDto != null) {
            return new RespTemplate(HttpStatus.OK, menuService.ModifyMenu(menuDto));
        }
        return new RespTemplate(HttpStatus.BAD_REQUEST, "");
    }

    @LogBack
    @ApiOperation(value = "添加菜单项")
    @RequestMapping(value = "/new_menu", method = RequestMethod.POST)
    public RespTemplate NewMenuIns(@Valid @RequestBody MenuDto menuDto) {
        if (menuDto != null) {
            return new RespTemplate(HttpStatus.OK, menuService.NewMenu(menuDto));
        }
        return new RespTemplate(HttpStatus.BAD_REQUEST, "");
    }

    @LogBack
    @ApiOperation(value = "通过ID删除菜单")
    @RequestMapping(value = "delete_menu/{id}", method = RequestMethod.DELETE)
    public RespTemplate DeleteOrgById(@PathVariable String id) {
        return new RespTemplate(HttpStatus.OK, menuService.DeleteMenuById(id));
    }

}