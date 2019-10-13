package com.csnight.redis.monitor.auth.service;

import com.csnight.redis.monitor.auth.jpa.SysUser;
import com.csnight.redis.monitor.auth.repos.SysRoleRepository;
import com.csnight.redis.monitor.auth.repos.SysUserRepository;
import org.springframework.stereotype.Service;

@Service
public class LoginUserService {
    private SysRoleRepository sysRoleRepository;
    private SysUserRepository userRepository;

    public LoginUserService(SysUserRepository userRepository, SysRoleRepository sysRoleRepository) {
        this.userRepository = userRepository;
        this.sysRoleRepository = sysRoleRepository;
    }

    public SysUser GetUserInfo(String username) {
        return userRepository.findByUsername(username);
    }
}
