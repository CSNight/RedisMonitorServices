package com.csnight.redis.monitor.db.repos;

import com.csnight.redis.monitor.db.jpa.SysOpLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysLogRepository extends JpaRepository<SysOpLog, String> {
}
