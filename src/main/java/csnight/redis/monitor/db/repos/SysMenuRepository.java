package csnight.redis.monitor.db.repos;

import csnight.redis.monitor.db.jpa.SysMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SysMenuRepository extends JpaRepository<SysMenu, Long>, JpaSpecificationExecutor<SysMenu> {
    List<SysMenu> findByPidOrderBySortAsc(Long id);

    @Query(value = "select hidden from sys_menu where pid=?", nativeQuery = true)
    List<Boolean> findHiddenByPid(Long pid);

    @Query(value = "select * from sys_menu where id=?", nativeQuery = true)
    SysMenu findOnly(Long id);

    SysMenu findByName(String name);

    @Query(value = "select * from sys_menu where component_name=?", nativeQuery = true)
    SysMenu findByComponent_name(String component_name);

    /**
     * 解绑角色菜单
     *
     * @param id 菜单ID
     */
    @Transactional
    @Modifying
    @Query(value = "delete from sys_role_menu where menu_id = ?", nativeQuery = true)
    void untiedMenu(Long id);
}
