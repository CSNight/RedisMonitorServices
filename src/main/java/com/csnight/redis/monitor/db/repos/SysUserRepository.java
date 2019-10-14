package com.csnight.redis.monitor.db.repos;

import com.csnight.redis.monitor.db.jpa.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SysUserRepository extends JpaRepository<SysUser, String> {
    SysUser findByUsernameOrEmail(String name, String email);

    SysUser findByEmail(String email);

    SysUser findByUsername(String name);

    SysUser findByPhone(String phone);

    List<SysUser> findAllByEnabled(boolean enable);
}
