package com.csnight.redis.monitor.db.repos;

import com.csnight.redis.monitor.db.jpa.SysRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysRoleRepository extends JpaRepository<SysRole, String> {
    SysRole findByName(String name);
}
