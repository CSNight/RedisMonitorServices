package com.csnight.redis.monitor.auth.repos;

import com.csnight.redis.monitor.auth.jpa.SysRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysRoleRepository extends JpaRepository<SysRole, String> {
    SysRole findByName(String name);
}
