package csnight.redis.monitor.rest.rms;

import csnight.redis.monitor.busi.rms.RmsInsRightsImpl;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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
}
