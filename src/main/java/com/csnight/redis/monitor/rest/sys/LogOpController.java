package com.csnight.redis.monitor.rest.sys;

import com.csnight.redis.monitor.busi.sys.OpLogServiceImpl;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("logs")
public class LogOpController {
    @Resource
    private OpLogServiceImpl opLogService;
}
