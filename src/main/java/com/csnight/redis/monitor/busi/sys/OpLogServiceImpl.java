package com.csnight.redis.monitor.busi.sys;

import com.csnight.redis.monitor.db.jpa.SysOpLog;
import com.csnight.redis.monitor.db.repos.SysLogRepository;
import com.csnight.redis.monitor.utils.GUID;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class OpLogServiceImpl {
    @Resource
    private SysLogRepository sysLogRepository;

    public SysLogRepository getSysLogRepository() {
        return sysLogRepository;
    }
}
