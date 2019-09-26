package com.csnight.redis.monitor.auth.repos;

import com.csnight.redis.monitor.auth.jpa.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysUserRepository extends JpaRepository<SysUser, String> {
    SysUser findByUsernameOrEmail(String name, String email);

    SysUser findByEmail(String email);

    SysUser findByUsername(String name);

    SysUser findByPhone(String phone);
}
