package com.csnight.redis.monitor.busi.sys;

import com.csnight.redis.monitor.db.jpa.SysOpLog;
import com.csnight.redis.monitor.db.repos.SysLogRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class OpLogServiceImpl {
    @Resource
    private SysLogRepository sysLogRepository;

    public void SaveAll(List<SysOpLog> logs) {
        sysLogRepository.saveAll(logs);
    }
}
