package com.csnight.redis.monitor.busi;

import com.csnight.redis.monitor.db.jpa.SysRole;
import com.csnight.redis.monitor.db.repos.SysRoleRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class RoleServiceImpl {
    @Resource
    private SysRoleRepository sysRoleRepository;

    public List<SysRole> GetAllRole() {
        return sysRoleRepository.findAll();
    }
}
