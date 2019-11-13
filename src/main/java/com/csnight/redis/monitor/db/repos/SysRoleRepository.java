package com.csnight.redis.monitor.db.repos;

import com.csnight.redis.monitor.db.jpa.SysPermission;
import com.csnight.redis.monitor.db.jpa.SysRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SysRoleRepository extends JpaRepository<SysRole, String>, JpaSpecificationExecutor<SysPermission> {
    SysRole findByName(String name);

    @Query(value = "select * from sys_role order by level", nativeQuery = true)
    List<SysRole> findAll();
}
