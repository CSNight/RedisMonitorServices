package csnight.redis.monitor.db.repos;

import csnight.redis.monitor.db.jpa.SysMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SysMenuRepository extends JpaRepository<SysMenu, Long>, JpaSpecificationExecutor<SysMenu> {
    List<SysMenu> findByPidOrderBySortAsc(Long id);

    @Query(value = "select hidden from rmsdb.sys_menu where pid=?", nativeQuery = true)
    List<Boolean> findHiddenByPid(Long pid);

    SysMenu findByName(String name);
    @Query(value = "select * from rmsdb.sys_menu where component_name=?", nativeQuery = true)
    SysMenu findByComponent_name(String component_name);
}
