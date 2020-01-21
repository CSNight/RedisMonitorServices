package csnight.redis.monitor.db.repos;

import csnight.redis.monitor.db.jpa.SysOrg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SysOrgRepository extends JpaRepository<SysOrg, Long>, JpaSpecificationExecutor<SysOrg> {
    List<SysOrg> findByPid(Long id);

    @Query(value = "select enabled from sys_org where pid=?", nativeQuery = true)
    List<Boolean> findEnabledByPid(Long pid);

    @Query(value = "select * from sys_org where id=?", nativeQuery = true)
    SysOrg findOnly(Long id);

    SysOrg findByName(String name);
}
