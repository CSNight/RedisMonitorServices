package csnight.redis.monitor.rest.rms;

import csnight.redis.monitor.aop.LogAsync;
import csnight.redis.monitor.busi.rms.RmsDataDumpImpl;
import csnight.redis.monitor.rest.rms.dto.DumpDto;
import csnight.redis.monitor.utils.RespTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;

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
    @ApiOperation("生成配置文件")
    @PreAuthorize("hasAuthority('DUMP_ADD_CONF')")
    @RequestMapping(value = "/shakeConf", method = RequestMethod.POST)
    public RespTemplate GenerateShakeConf(@Valid @RequestBody DumpDto dto) throws IOException {
        return new RespTemplate(HttpStatus.OK, rmsDataDump.NewShakeConf(dto));
    }

    @LogAsync(module = "DUMP", auth = "DUMP_DEL_RECORD")
    @ApiOperation("删除操作记录")
    @PreAuthorize("hasAuthority('DUMP_DEL_RECORD')")
    @RequestMapping(value = "/shakeRecord/{cid}", method = RequestMethod.DELETE)
    public RespTemplate DeleteShakeRecord(@PathVariable String cid) {
        return new RespTemplate(HttpStatus.OK, rmsDataDump.DeleteRecord(cid));
    }

    @LogAsync(module = "DUMP", auth = "DUMP_GET_RECORD")
    @ApiOperation("查询操作记录")
    @PreAuthorize("hasAuthority('DUMP_GET_RECORD')")
    @RequestMapping(value = "/shakeRecords", method = RequestMethod.GET)
    public RespTemplate GetShakeRecords() {
        return new RespTemplate(HttpStatus.OK, rmsDataDump.GetAll());
    }

    @LogAsync(module = "DUMP", auth = "DUMP_GET_RECORD")
    @ApiOperation("根据用户查询操作记录")
    @PreAuthorize("hasAuthority('DUMP_GET_RECORD')")
    @RequestMapping(value = "/shakeRecordsByUser", method = RequestMethod.GET)
    public RespTemplate GetShakeRecordsByUser() {
        return new RespTemplate(HttpStatus.OK, rmsDataDump.GetByUser());
    }
}
