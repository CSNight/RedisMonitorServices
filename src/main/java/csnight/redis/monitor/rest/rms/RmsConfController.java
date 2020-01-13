package csnight.redis.monitor.rest.rms;

import csnight.redis.monitor.aop.LogAsync;
import csnight.redis.monitor.busi.rms.RmsConfSetImpl;
import csnight.redis.monitor.exception.ConfigException;
import csnight.redis.monitor.rest.rms.dto.ConfigDto;
import csnight.redis.monitor.utils.RespTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Map;

/**
 * @author csnight
 * @description Redis配置管理API
 * @since 2020-1-12 15:48
 */
@RestController
@RequestMapping("config")
@Api(tags = "Redis配置管理API")
public class RmsConfController {
    @Resource
    private RmsConfSetImpl rmsConfSet;

    @LogAsync(module = "CONF", auth = "RMS_CONF_GET")
    @ApiOperation("Redis配置查询")
    @PreAuthorize("hasAuthority('RMS_CONF_GET')")
    @RequestMapping(value = "/configs/{ins_id}", method = RequestMethod.GET)
    public RespTemplate GetInstanceConfig(@PathVariable String ins_id) throws ConfigException {
        Map<String, Object> res = rmsConfSet.GetConfig(ins_id);
        if (res == null) {
            return new RespTemplate(HttpStatus.BAD_REQUEST, "Please check redis role and connection");
        }
        return new RespTemplate(HttpStatus.OK, res);
    }

    @LogAsync(module = "CONF", auth = "RMS_CONF_SAVE")
    @ApiOperation("Redis配置查询")
    @PreAuthorize("hasAuthority('RMS_CONF_SAVE')")
    @RequestMapping(value = "/configs", method = RequestMethod.PUT)
    public RespTemplate SaveInstanceConfig(@Valid @RequestBody ConfigDto dto) throws ConfigException {
        String res = rmsConfSet.SaveConfig(dto);
        if (res == null) {
            return new RespTemplate(HttpStatus.BAD_REQUEST, "Please check redis role and connection");
        }
        return new RespTemplate(HttpStatus.OK, res);
    }
}
