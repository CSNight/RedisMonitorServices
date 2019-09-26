package com.csnight.redis.monitor.auth.repos;

import com.csnight.redis.monitor.auth.jpa.PersistentLogins;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersistentRepository extends JpaRepository<PersistentLogins, String> {
    PersistentLogins findByUsername(String name);
}
