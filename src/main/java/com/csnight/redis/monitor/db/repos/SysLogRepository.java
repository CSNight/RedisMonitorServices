package com.csnight.redis.monitor.db.repos;

import com.csnight.redis.monitor.db.jpa.SysOpLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import javax.persistence.criteria.Predicate;

public interface SysLogRepository extends JpaRepository<SysOpLog, String>, JpaSpecificationExecutor<SysOpLog> {
    Page<SysOpLog> findByUn(String username, Pageable pageable);
}
