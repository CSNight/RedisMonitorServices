package com.csnight.redis.monitor.controller;

import com.csnight.redis.monitor.auth.impl.UserDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/sign")
    public String login() {
        return "sign";
    }


}
