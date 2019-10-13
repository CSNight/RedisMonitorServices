package com.csnight.redis.monitor.controller;

import com.csnight.redis.monitor.auth.jpa.SysUser;
import com.csnight.redis.monitor.auth.service.LoginUserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
    private final LoginUserService loginUserService;

    public IndexController(LoginUserService loginUserService) {
        this.loginUserService = loginUserService;
    }

    @GetMapping("/")
    public String index(Model model) {
        if (!SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString().equals("anonymousUser")) {
            String name = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
            model.addAttribute("user", name);
        }
        return "index";
    }

    @GetMapping("/main")
    public String main(Model model) {
        if (!SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString().equals("anonymousUser")) {
            String name = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
            SysUser sysUser = loginUserService.GetUserInfo(name);
            model.addAttribute("user", name);
            model.addAttribute("head", new String(sysUser.getHead_img()));
        }
        return "main";
    }
}
