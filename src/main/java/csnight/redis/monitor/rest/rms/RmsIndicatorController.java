package csnight.redis.monitor.rest.rms;

import csnight.redis.monitor.aop.LogAsync;
import csnight.redis.monitor.busi.rms.RmsIndicatorImpl;
import csnight.redis.monitor.rest.rms.dto.IndicatorDto;
import csnight.redis.monitor.utils.RespTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("quota")
@Api(tags = "Redis监控指标API")
public class RmsIndicatorController {
    @Resource
    private RmsIndicatorImpl rmsIndicator;

    @LogAsync(module = "INDICATOR", auth = "INDICATOR_QUERY")
    @ApiOperation("查询Redis监控指标")
    @PreAuthorize("hasAuthority('INDICATOR_QUERY')")
    @RequestMapping(value = "/indicator", method = RequestMethod.GET)
    public RespTemplate GetAllIndicator() {
        return new RespTemplate(HttpStatus.OK, rmsIndicator.GetIndicators());
    }

    @LogAsync(module = "INDICATOR", auth = "INDICATOR_ADD")
    @ApiOperation("新增Redis监控指标")
    @PreAuthorize("hasAuthority('INDICATOR_ADD')")
    @RequestMapping(value = "/indicator", method = RequestMethod.POST)
    public RespTemplate AddIndicator(@Valid @RequestBody IndicatorDto dto) {
        return new RespTemplate(HttpStatus.OK, rmsIndicator.AddIndicator(dto));
    }

    @LogAsync(module = "INDICATOR", auth = "INDICATOR_UPDATE")
    @ApiOperation("更新Redis监控指标")
    @PreAuthorize("hasAuthority('INDICATOR_UPDATE')")
    @RequestMapping(value = "/indicator", method = RequestMethod.PUT)
    public RespTemplate UpdateIndicator(@Valid @RequestBody IndicatorDto dto) {
        return new RespTemplate(HttpStatus.OK, rmsIndicator.UpdateIndicator(dto));
    }

    @LogAsync(module = "INDICATOR", auth = "INDICATOR_DELETE")
    @ApiOperation("删除Redis监控指标")
    @PreAuthorize("hasAuthority('INDICATOR_DELETE')")
    @RequestMapping(value = "/indicator/{id}", method = RequestMethod.DELETE)
    public RespTemplate DeleteIndicator(@PathVariable String id) {
        return new RespTemplate(HttpStatus.OK, rmsIndicator.DeleteIndicator(id));
    }
}
