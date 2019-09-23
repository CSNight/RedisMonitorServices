package com.csnight.redis.monitor.auth.repos;

import com.csnight.redis.monitor.auth.jpa.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface SysUserRepository extends JpaRepository<SysUser, String> {
    public SysUser findByUsernameOrEmail(String name, String email);

    public SysUser findByEmail(String email);

    public SysUser findByUsername(String name);
}
