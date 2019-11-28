package com.redis.monitor.busi.rms;

import com.redis.monitor.db.jpa.RmsInstance;
import com.redis.monitor.db.repos.RmsInsRepository;
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
