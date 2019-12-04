package csnight.redis.monitor.db.repos;

import csnight.redis.monitor.db.jpa.SysPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SysPermissionRepository extends JpaRepository<SysPermission, String>, JpaSpecificationExecutor<SysPermission> {
    SysPermission findByName(String name);
}
