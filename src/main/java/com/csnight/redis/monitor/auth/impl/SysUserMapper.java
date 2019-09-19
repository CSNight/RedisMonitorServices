package com.csnight.redis.monitor.auth.impl;

import com.csnight.redis.monitor.auth.jpa.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysUserMapper extends JpaRepository<SysUser, String> {
    public SysUser findByUsername(String name);
}
