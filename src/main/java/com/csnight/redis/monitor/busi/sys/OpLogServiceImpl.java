package com.csnight.redis.monitor.busi.sys;

import com.csnight.redis.monitor.busi.sys.exp.OpLogQueryExp;
import com.csnight.redis.monitor.db.blurry.QueryAnnotationProcess;
import com.csnight.redis.monitor.db.jpa.SysOpLog;
import com.csnight.redis.monitor.db.jpa.SysUser;
import com.csnight.redis.monitor.db.repos.SysLogRepository;
import com.csnight.redis.monitor.db.repos.SysUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class OpLogServiceImpl {
    @Resource
    private SysLogRepository sysLogRepository;

    @Resource
    private SysUserRepository userRepository;

    public void SaveAll(List<SysOpLog> logs) {
        sysLogRepository.saveAll(logs);
    }


    public Page<SysOpLog> GetLogsByUser(String username, int cur, int size) {
        Sort s = Sort.by(Sort.Direction.ASC, "ct");
        cur = cur - 1;
        Pageable pageable = PageRequest.of(cur, size, s);
        return sysLogRepository.findByUn(username, pageable);
    }

    public Page<SysOpLog> QueryLogsByCond(OpLogQueryExp exp) {
        SysUser user = userRepository.findByUsername(exp.getUn());
        if (user != null) {
            Sort sort;
            if (exp.getSort() == null) {
                sort = Sort.by(Sort.Direction.ASC, "ct");
            } else {
                sort = Sort.by(Sort.Direction.fromString(exp.getDirect()), exp.getSort());
            }
            int cur = exp.getCur() - 1;
            int size = exp.getSize();
            if (cur < 0) {
                cur = 0;
            }
            if (exp.getSize() < 1) {
                size = 5;
            }
            Pageable pageable = PageRequest.of(cur, size, sort);
            return sysLogRepository.findAll((root, criteriaQuery, criteriaBuilder) ->
                    QueryAnnotationProcess.getPredicate(root, exp, criteriaBuilder), pageable);
        }
        return null;
    }
}
