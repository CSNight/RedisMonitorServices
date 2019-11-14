package com.csnight.redis.monitor.db.repos;

import com.csnight.redis.monitor.db.jpa.SysRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SysRoleRepository extends JpaRepository<SysRole, String>, JpaSpecificationExecutor<SysRole> {
    SysRole findByName(String name);

    @Query(value = "select * from rmsdb.sys_role order by level", nativeQuery = true)
    List<SysRole> findAll();

    SysRole findByCode(String name);
}
