package com.redis.monitor.rest.rms;

import com.redis.monitor.busi.rms.TestDBImpl;
import com.redis.monitor.utils.RespTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("test")
@Api(tags = "测试数据API")
public class TestController {

    @Resource
    private TestDBImpl testDB;

    @ApiOperation("test")
    @RequestMapping(value = "test", method = RequestMethod.GET)
    public RespTemplate Test() {
        return new RespTemplate(HttpStatus.OK, testDB.GetInstances());
    }
}
