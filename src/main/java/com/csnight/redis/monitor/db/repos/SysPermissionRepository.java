package com.csnight.redis.monitor.db.repos;

import com.csnight.redis.monitor.db.jpa.SysPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SysPermissionRepository extends JpaRepository<SysPermission, String>, JpaSpecificationExecutor<SysPermission> {
    SysPermission findByName(String name);

    @Query(value = "select * from sys_permission order by name", nativeQuery = true)
    List<SysPermission> findAll();
}
