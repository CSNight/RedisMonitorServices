package com.csnight.redis.monitor.controller.rest;

import com.csnight.redis.monitor.busi.UserServiceImpl;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private UserServiceImpl userService;

    public UserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @RequestMapping("/org")
    public String get_org() {
        return userService.GetOrgTree();
    }
    @RequestMapping("/orgs")
    public String get_orgbypid() {
        return userService.DeleteOrgById("1");
    }
}
