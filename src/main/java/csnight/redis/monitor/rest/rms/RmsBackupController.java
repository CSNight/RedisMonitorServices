package csnight.redis.monitor.rest.rms;

import csnight.redis.monitor.aop.LogAsync;
import csnight.redis.monitor.busi.rms.RmsDataBackupImpl;
import csnight.redis.monitor.utils.RespTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author csnight
 * @description
 * @since 2020/1/20 14:48
 */
@RestController
@RequestMapping("backup")
@Api(tags = "Redis备份管理API")
public class RmsBackupController {
    @Resource
    private RmsDataBackupImpl dataBackup;

    @LogAsync(module = "BACKUP", auth = "BACKUP_QUERY")
    @ApiOperation("根据用户查询备份文件")
    @PreAuthorize("hasAuthority('BACKUP_QUERY')")
    @RequestMapping(value = "/getByUser", method = RequestMethod.GET)
    public RespTemplate GetBackups() {
        return new RespTemplate(HttpStatus.OK, dataBackup.GetDataRecord());
    }

    @LogAsync(module = "BACKUP", auth = "BACKUP_QUERY_ALL")
    @ApiOperation("查询全部备份文件")
    @PreAuthorize("hasAuthority('BACKUP_QUERY_ALL')")
    @RequestMapping(value = "/getAll", method = RequestMethod.GET)
    public RespTemplate GetBackupsAll() {
        return new RespTemplate(HttpStatus.OK, dataBackup.GetAllDataRecord());
    }

    @LogAsync(module = "BACKUP", auth = "BACKUP_QUERY_ID")
    @ApiOperation("根据ID查询备份文件")
    @PreAuthorize("hasAuthority('DUMP_QUERY')")
    @RequestMapping(value = "/getById/{id}", method = RequestMethod.GET)
    public RespTemplate GetBackupById(@PathVariable String id) {
        return new RespTemplate(HttpStatus.OK, dataBackup.GetDataRecordById(id));
    }

    @LogAsync(module = "BACKUP", auth = "BACKUP_DEL")
    @ApiOperation("根据ID删除备份文件")
    @PreAuthorize("hasAuthority('BACKUP_DEL')")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
    public RespTemplate DeleteBackupsById(@PathVariable String id) {
        return new RespTemplate(HttpStatus.OK, dataBackup.DeleteById(id));
    }
}
