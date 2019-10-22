package com.csnight.redis.monitor.rest;

import com.alibaba.fastjson.JSONObject;
import com.csnight.redis.monitor.aop.LogBack;
import com.csnight.redis.monitor.busi.OrgServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping(value = "org")
@Api(tags = "组织机构API")
public class OrgController {

    private OrgServiceImpl userService;

    public OrgController(OrgServiceImpl userService) {
        this.userService = userService;
    }

    @LogBack
    @ApiOperation(value = "获取组织机构目录树")
    @RequestMapping(value = "/get_org_tree", method = RequestMethod.GET)
    public String GetOrgTree() {
        return userService.GetOrgTree();
    }

    @LogBack
    @ApiOperation(value = "获取组织机构目录列表")
    @RequestMapping(value = "/get_org_list", method = RequestMethod.GET)
    public String GetOrgList() {
        return userService.GetOrgList();
    }

    @LogBack
    @ApiOperation(value = "通过父节点ID获取组织机构目录")
    @RequestMapping(value = "/{pid}/get_org", method = RequestMethod.GET)
    public String GetOrgByPid(@PathVariable String pid) {
        return userService.GetOrgByPid(pid);
    }

    @LogBack
    @ApiOperation(value = "修改组织机构")
    @RequestMapping(value = "/modify_org", method = RequestMethod.PUT)
    public String ModifyOrgIns(@RequestParam("org_ent") String org_ent) {
        if (org_ent != null && !org_ent.equals("")) {
            return userService.ModifyOrg(JSONObject.parseObject(org_ent));
        }
        return "";
    }

    @LogBack
    @ApiOperation(value = "添加组织机构")
    @ApiImplicitParam(paramType = "query", name = "org_ent", value = "新组织", required = true, dataType = "String")
    @RequestMapping(value = "/new_org", method = RequestMethod.POST)
    public String NewOrgIns(@RequestParam("org_ent") String org_ent) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String name = userDetails.getUsername();
        if (org_ent != null && !org_ent.equals("")) {
            return userService.NewOrg(JSONObject.parseObject(org_ent),name);
        }
        return "";
    }

    @LogBack
    @ApiOperation(value = "通过ID删除组织机构")
    @RequestMapping(value = "delete_org/{id}", method = RequestMethod.DELETE)
    public String DeleteOrgById(@PathVariable String id) {
        return userService.DeleteOrgById(id);
    }
}
