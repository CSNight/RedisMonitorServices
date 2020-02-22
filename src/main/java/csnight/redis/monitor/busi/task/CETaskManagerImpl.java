package csnight.redis.monitor.busi.task;

import csnight.redis.monitor.db.repos.RmsInsRepository;
import csnight.redis.monitor.db.repos.RmsJobRepository;
import csnight.redis.monitor.quartz.JobFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class CETaskManagerImpl {
    @Resource
    private RmsJobRepository jobRepository;
    @Resource
    private RmsInsRepository insRepository;
    @Resource
    private JobFactory jobFactory;
}
