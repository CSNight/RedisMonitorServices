package com.csnight.redis.monitor.db.repos;

import com.csnight.redis.monitor.db.jpa.SysMenu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SysMenuRepository extends JpaRepository<SysMenu, Long> {
    List<SysMenu> findByPid(Long id);
}
