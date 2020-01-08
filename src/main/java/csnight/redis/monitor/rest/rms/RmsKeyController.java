package csnight.redis.monitor.rest.rms;

import csnight.redis.monitor.aop.LogAsync;
import csnight.redis.monitor.busi.rms.RmsKeyManageImpl;
import csnight.redis.monitor.exception.ConfigException;
import csnight.redis.monitor.rest.rms.dto.KeyEntDto;
import csnight.redis.monitor.rest.rms.dto.KeyScanDto;
import csnight.redis.monitor.utils.RespTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("keys")
@Api(tags = "Redis键值管理API")
public class RmsKeyController {
    @Resource
    private RmsKeyManageImpl keyManage;

    @LogAsync(module = "KEYS", auth = "KEYS_KEY_SCAN")
    @ApiOperation("检索实例库键")
    @PreAuthorize("hasAuthority('KEYS_KEY_SCAN')")
    @RequestMapping(value = "/scanner", method = RequestMethod.GET)
    public RespTemplate GetInstanceKeys(@Valid KeyScanDto dto) throws ConfigException {
        Map<String, Object> res = keyManage.GetDBKeys(dto);
        if (res == null) {
            return new RespTemplate(HttpStatus.BAD_REQUEST, "Can not get any key, please check redis role and connection");
        }
        return new RespTemplate(HttpStatus.OK, res);
    }

    @LogAsync(module = "KEYS", auth = "KEYS_KEY_VAL")
    @ApiOperation("检索实例库键值")
    @PreAuthorize("hasAuthority('KEYS_KEY_VAL')")
    @RequestMapping(value = "/keyvalue", method = RequestMethod.GET)
    public RespTemplate GetInsKeyValue(@Valid KeyEntDto dto) {
        Map<String, Object> res = keyManage.GetDBKeyValue(dto);
        if (res == null) {
            return new RespTemplate(HttpStatus.BAD_REQUEST, "Can not get any key, please check redis role and connection");
        }
        return new RespTemplate(HttpStatus.OK, res);
    }

    @LogAsync(module = "KEYS", auth = "KEYS_KEY_REFRESH")
    @ApiOperation("刷新键信息")
    @PreAuthorize("hasAuthority('KEYS_KEY_REFRESH')")
    @RequestMapping(value = "/refresh", method = RequestMethod.GET)
    public RespTemplate RefreshKey(@Valid KeyEntDto dto) {
        Map<String, Object> res = keyManage.RefreshKey(dto);
        if (res == null) {
            return new RespTemplate(HttpStatus.BAD_REQUEST, "Can not get any key, please check redis role and connection");
        }
        return new RespTemplate(HttpStatus.OK, res);
    }

    @LogAsync(module = "KEYS", auth = "KEYS_KEY_EXPIRE")
    @ApiOperation("设置键过期时间")
    @PreAuthorize("hasAuthority('KEYS_KEY_EXPIRE')")
    @RequestMapping(value = "/expires/{type}", method = RequestMethod.PUT)
    public RespTemplate InsKeyExpire(@Valid @RequestBody KeyEntDto dto, @PathVariable String type) {
        String res = keyManage.SetKeysExpire(dto, type);
        return new RespTemplate(HttpStatus.OK, res);
    }

    @LogAsync(module = "KEYS", auth = "KEYS_KEY_DELETE")
    @ApiOperation("删除指定键")
    @PreAuthorize("hasAuthority('KEYS_KEY_DELETE')")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public RespTemplate InsKeyDelete(@Valid @RequestBody KeyEntDto dto) {
        String res = keyManage.DeleteKeys(dto);
        return new RespTemplate(HttpStatus.OK, res);
    }
}
