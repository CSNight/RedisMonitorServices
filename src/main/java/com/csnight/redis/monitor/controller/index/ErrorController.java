package com.csnight.redis.monitor.controller.index;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {
    @GetMapping("/404")
    public String pageNotFount() {
        return "404";
    }

    @GetMapping("/500")
    public String internalError() {
        return "500";
    }

    @GetMapping("/403")
    public String forbidden() {
        return "403";
    }
}
