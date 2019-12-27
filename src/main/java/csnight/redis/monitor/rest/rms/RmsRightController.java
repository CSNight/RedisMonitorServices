package csnight.redis.monitor.rest.rms;

import csnight.redis.monitor.aop.LogAsync;
import csnight.redis.monitor.busi.rms.RmsInsRightsImpl;
import csnight.redis.monitor.exception.ConflictsException;
import csnight.redis.monitor.rest.rms.dto.InsRightsDto;
import csnight.redis.monitor.utils.RespTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @author csnight
 * @description 实例授权
 * @since 2019/12/27 09:44
 */
@RestController
@RequestMapping("rights")
@Api(tags = "Redis实例授权API")
public class RmsRightController {
    @Resource
    private RmsInsRightsImpl rmsInsRights;

    @LogAsync(module = "INS_AUTH", auth = "AUTH_QUERY_ALL")
    @ApiOperation("查询Redis授权列表")
    @PreAuthorize("hasAuthority('AUTH_QUERY_ALL')")
    @RequestMapping(value = "/get_auth_list", method = RequestMethod.GET)
    public RespTemplate GetAllAuthorizes() {
        return new RespTemplate(HttpStatus.OK, rmsInsRights.GetPermitsList());
    }

    @LogAsync(module = "INS_AUTH", auth = "AUTH_QUERY")
    @ApiOperation("查询用户Redis授权列表")
    @PreAuthorize("hasAuthority('AUTH_QUERY')")
    @RequestMapping(value = "/get_auth_list/{user_id}", method = RequestMethod.GET)
    public RespTemplate GetUserAuthorizes(@PathVariable String user_id) {
        return new RespTemplate(HttpStatus.OK, rmsInsRights.GetBelongs(user_id));
    }

    @LogAsync(module = "INS_AUTH", auth = "AUTH_ADD")
    @ApiOperation("新建Redis实例授权")
    @PreAuthorize("hasAuthority('AUTH_ADD')")
    @RequestMapping(value = "/new_auth", method = RequestMethod.POST)
    public RespTemplate AddAuthorize(@Valid @RequestBody InsRightsDto dto) throws ConflictsException {
        return new RespTemplate(HttpStatus.OK, rmsInsRights.AddInsPermits(dto));
    }

    @LogAsync(module = "INS_AUTH", auth = "AUTH_UPDATE")
    @ApiOperation("修改Redis实例授权")
    @PreAuthorize("hasAuthority('AUTH_UPDATE')")
    @RequestMapping(value = "/modify_auth", method = RequestMethod.PUT)
    public RespTemplate ModifyInsAuth(@Valid @RequestBody InsRightsDto dto) {
        return new RespTemplate(HttpStatus.OK, rmsInsRights.ModifyInsRight(dto));
    }

    @LogAsync(module = "INS_AUTH", auth = "AUTH_DEL")
    @ApiOperation("删除Redis实例授权")
    @PreAuthorize("hasAuthority('AUTH_DEL')")
    @RequestMapping(value = "/delete_auth/{id}", method = RequestMethod.DELETE)
    public RespTemplate DeleteInsAuth(@PathVariable String id) {
        return new RespTemplate(HttpStatus.OK, rmsInsRights.DeleteInsRight(id));
    }
}
