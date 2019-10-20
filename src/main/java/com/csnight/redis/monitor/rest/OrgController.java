package com.csnight.redis.monitor.rest;

import com.alibaba.fastjson.JSONObject;
import com.csnight.redis.monitor.aop.LogBack;
import com.csnight.redis.monitor.busi.OrgServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

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
    @RequestMapping(value = "/new_org", method = RequestMethod.POST)
    public String NewOrgIns(@RequestParam("org_ent") String org_ent) {
        if (org_ent != null && !org_ent.equals("")) {
            return userService.NewOrg(JSONObject.parseObject(org_ent));
        }
        return "";
    }

    @LogBack
    @ApiOperation(value = "通过ID删除组织机构")
    @RequestMapping(value = "{id}/delete_org/", method = RequestMethod.DELETE)
    public String DeleteOrgById(@PathVariable String id) {
        return userService.DeleteOrgById(id);
    }
}
