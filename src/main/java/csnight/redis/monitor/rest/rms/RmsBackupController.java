package csnight.redis.monitor.rest.rms;

import csnight.redis.monitor.busi.rms.RmsDataBackupImpl;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
