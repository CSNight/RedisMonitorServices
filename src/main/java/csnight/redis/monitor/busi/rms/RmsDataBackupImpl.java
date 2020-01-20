package csnight.redis.monitor.busi.rms;

import csnight.redis.monitor.db.repos.RmsDataRecRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author csnight
 * @description
 * @since 2020/1/20 14:43
 */
@Service
public class RmsDataBackupImpl {
    @Resource
    private RmsDataRecRepository dataRecRepository;


}
