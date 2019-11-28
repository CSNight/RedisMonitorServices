package com.redis.monitor.db.repos;

import com.redis.monitor.db.jpa.RmsInstance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RmsInsRepository extends JpaRepository<RmsInstance, String> {
}
