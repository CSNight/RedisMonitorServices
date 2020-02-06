package csnight.redis.monitor.redis.statistic;

import csnight.redis.monitor.db.repos.RmsRcsRepository;
import csnight.redis.monitor.db.repos.RmsRksRepository;
import csnight.redis.monitor.db.repos.RmsRosRepository;
import csnight.redis.monitor.db.repos.RmsRpsRepository;
import csnight.redis.monitor.utils.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MysqlRmsLogExecutorImpl implements RmsLogsExecutor {
    private RmsRpsRepository rpsRepository = ReflectUtils.getBean(RmsRpsRepository.class);
    private RmsRcsRepository rcsRepository = ReflectUtils.getBean(RmsRcsRepository.class);
    private RmsRosRepository rosRepository = ReflectUtils.getBean(RmsRosRepository.class);
    private RmsRksRepository rksRepository = ReflectUtils.getBean(RmsRksRepository.class);
    private Logger _log = LoggerFactory.getLogger(FileRmsLogExecutorImpl.class);

    public MysqlRmsLogExecutorImpl() {
        _log.info("Mysql redis statistic log executors initialize");
    }
}
