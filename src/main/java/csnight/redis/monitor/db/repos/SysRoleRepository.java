package csnight.redis.monitor.db.repos;

import csnight.redis.monitor.db.jpa.SysRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SysRoleRepository extends JpaRepository<SysRole, String>, JpaSpecificationExecutor<SysRole> {
    SysRole findByName(String name);

    SysRole findByCode(String name);
}
