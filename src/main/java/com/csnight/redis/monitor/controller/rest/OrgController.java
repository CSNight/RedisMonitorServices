package com.csnight.redis.monitor.controller.rest;

import com.alibaba.fastjson.JSONObject;
import com.csnight.redis.monitor.busi.OrgServiceImpl;
import com.csnight.redis.monitor.log.LogBack;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "org")
public class OrgController {

    private OrgServiceImpl userService;

    public OrgController(OrgServiceImpl userService) {
        this.userService = userService;
    }

    @LogBack
    @RequestMapping(value = "/get_org_tree", method = RequestMethod.GET)
    public String GetOrgTree() {
        return userService.GetOrgTree();
    }

    @LogBack
    @RequestMapping(value = "/{pid}/get_org", method = RequestMethod.GET)
    public String GetOrgByPid(@PathVariable String pid) {
        return userService.GetOrgByPid(pid);
    }

    @LogBack
    @RequestMapping(value = "/modify_org", method = RequestMethod.POST)
    public String ModifyOrgIns(@RequestParam("org_ent") String org_ent) {
        if (org_ent != null && !org_ent.equals("")) {
            return userService.ModifyOrg(JSONObject.parseObject(org_ent));
        }
        return "";
    }

    @RequestMapping(value = "{id}/delete_org/", method = RequestMethod.DELETE)
    public String DeleteOrgById(@PathVariable String id) {
        return userService.DeleteOrgById(id);
    }
}
