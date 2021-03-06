package csnight.redis.monitor.redis.statistic;

import csnight.redis.monitor.db.jpa.*;
import csnight.redis.monitor.db.repos.RmsRcsRepository;
import csnight.redis.monitor.db.repos.RmsRksRepository;
import csnight.redis.monitor.db.repos.RmsRosRepository;
import csnight.redis.monitor.db.repos.RmsRpsRepository;
import csnight.redis.monitor.utils.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MysqlRmsLogExecutorImpl implements RmsLogsExecutor {
    private RmsRpsRepository rpsRepository = ReflectUtils.getBean(RmsRpsRepository.class);
    private RmsRcsRepository rcsRepository = ReflectUtils.getBean(RmsRcsRepository.class);
    private RmsRosRepository rosRepository = ReflectUtils.getBean(RmsRosRepository.class);
    private RmsRksRepository rksRepository = ReflectUtils.getBean(RmsRksRepository.class);
    private Logger _log = LoggerFactory.getLogger(FileRmsLogExecutorImpl.class);
    private boolean isAccessible = true;

    public MysqlRmsLogExecutorImpl() {
        _log.info("Mysql redis statistic log executors initialize success");
    }

    @Override
    public boolean isAccessible() {
        return isAccessible;
    }

    @Override
    public boolean execute(List<RmsLog> logs) {
        List<RmsRpsLog> pLogs = new ArrayList<>();
        List<RmsRosLog> oLogs = new ArrayList<>();
        List<RmsRcsLog> cLogs = new ArrayList<>();
        List<RmsRksLog> kLogs = new ArrayList<>();
        for (RmsLog log : logs) {
            switch (log.getSector()) {
                case "Physical":
                    pLogs.add((RmsRpsLog) log);
                    break;
                case "Commands":
                    oLogs.add((RmsRosLog) log);
                    break;
                case "Clients":
                    cLogs.add((RmsRcsLog) log);
                    break;
                case "Keyspace":
                    kLogs.add((RmsRksLog) log);
                    break;
            }
        }
        try {
            if (pLogs.size() > 0) {
                rpsRepository.saveAll(pLogs);
            }
            if (pLogs.size() > 0) {
                rosRepository.saveAll(oLogs);
            }
            if (pLogs.size() > 0) {
                rcsRepository.saveAll(cLogs);
            }
            if (pLogs.size() > 0) {
                rksRepository.saveAll(kLogs);
            }
        } catch (Exception ex) {
            isAccessible = false;
        }
        pLogs.clear();
        oLogs.clear();
        cLogs.clear();
        kLogs.clear();
        pLogs = null;
        oLogs = null;
        cLogs = null;
        kLogs = null;
        return true;
    }

    @Override
    public void checkAccess() {
    }

    @Override
    public boolean destroy() {
        return true;
    }
}
