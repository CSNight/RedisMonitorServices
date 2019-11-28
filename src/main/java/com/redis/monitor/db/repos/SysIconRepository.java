package com.redis.monitor.db.repos;

import com.redis.monitor.db.jpa.SysIcons;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysIconRepository extends JpaRepository<SysIcons, Long> {
}
