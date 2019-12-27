package csnight.redis.monitor.db.repos;

import csnight.redis.monitor.db.jpa.SysRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface SysRoleRepository extends JpaRepository<SysRole, String>, JpaSpecificationExecutor<SysRole> {
    @Query(value = "select * from sys_role where id=?", nativeQuery = true)
    SysRole findOnly(String id);

    SysRole findByName(String name);

    SysRole findByCode(String name);
}
