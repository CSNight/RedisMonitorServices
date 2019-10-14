package com.csnight.redis.monitor.db.repos;

import com.csnight.redis.monitor.db.jpa.SysOrg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SysOrgRepository extends JpaRepository<SysOrg, String> {
    List<SysOrg> findByPid(String id);
}
