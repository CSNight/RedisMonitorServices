package csnight.redis.monitor.db.repos;

import csnight.redis.monitor.db.jpa.SysPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface SysPermissionRepository extends JpaRepository<SysPermission, String>, JpaSpecificationExecutor<SysPermission> {
    SysPermission findByName(String name);

    /**
     * 解绑角色权限
     *
     * @param id 菜单ID
     */
    @Transactional
    @Modifying
    @Query(value = "delete from sys_role_permission where permission_id = ?", nativeQuery = true)
    void untiedPermission(String id);
}
