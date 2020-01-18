package csnight.redis.monitor.rest.rms;

import csnight.redis.monitor.aop.LogAsync;
import csnight.redis.monitor.busi.rms.RmsDataDumpImpl;
import csnight.redis.monitor.rest.rms.dto.DumpDto;
import csnight.redis.monitor.utils.RespTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @author csnight
 * @description
 * @since 2020-1-18 21:55
 */
@RestController
@RequestMapping("dump")
@Api(tags = "Redis数据管理API")
public class RmsShakeController {
    @Resource
    private RmsDataDumpImpl rmsDataDump;

    @LogAsync(module = "DUMP", auth = "DUMP_ADD_CONF")
    @ApiOperation("检索实例库键")
    @PreAuthorize("hasAuthority('DUMP_ADD_CONF')")
    @RequestMapping(value = "/shakeConf", method = RequestMethod.POST)
    public RespTemplate GetInstanceKeys(@Valid DumpDto dto) {
        return new RespTemplate(HttpStatus.OK, rmsDataDump.NewShakeConf(dto));
    }
}
