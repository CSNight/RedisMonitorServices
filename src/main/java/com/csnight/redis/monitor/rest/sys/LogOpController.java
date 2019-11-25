package com.csnight.redis.monitor.rest.sys;

import com.csnight.redis.monitor.busi.sys.OpLogServiceImpl;
import com.csnight.redis.monitor.busi.sys.exp.OpLogQueryExp;
import com.csnight.redis.monitor.db.jpa.SysOpLog;
import com.csnight.redis.monitor.utils.RespTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("logs")
@Api(tags = "操作日志API")
public class LogOpController {
    @Resource
    private OpLogServiceImpl opLogService;

    //@PreAuthorize("hasAuthority('MENU_ICONS')")
    @ApiOperation(value = "获取用户日志")
    @RequestMapping(value = "/get_logs/{user}/{cur}/{size}", method = RequestMethod.GET)
    public RespTemplate GetLogsByUser(@PathVariable String user, @PathVariable int cur, @PathVariable int size) {
        return new RespTemplate(HttpStatus.OK, opLogService.GetLogsByUser(user, cur, size));
    }

    @ApiOperation(value = "根据条件查询用户日志")
    @RequestMapping(value = "/query_logs", method = RequestMethod.GET)
    public RespTemplate QueryLogsByUser(OpLogQueryExp exp) {
        Page<SysOpLog> res = opLogService.QueryLogsByCond(exp);
        if (res != null) {
            return new RespTemplate(HttpStatus.OK, res);
        }
        return new RespTemplate(400, HttpStatus.NOT_FOUND, "User Not Found", "", "");
    }
}
