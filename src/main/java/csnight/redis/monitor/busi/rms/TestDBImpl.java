package csnight.redis.monitor.busi.rms;

import csnight.redis.monitor.db.jpa.RmsInstance;
import csnight.redis.monitor.db.repos.RmsInsRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class TestDBImpl {
    @Resource
    private RmsInsRepository rmsInsRepository;

    public List<RmsInstance> GetInstances() {
        return rmsInsRepository.findAll();
    }

}
