package csnight.redis.monitor.busi.sys;

import csnight.redis.monitor.busi.sys.exp.PermitQueryExp;
import csnight.redis.monitor.db.blurry.QueryAnnotationProcess;
import csnight.redis.monitor.db.jpa.SysMenu;
import csnight.redis.monitor.db.jpa.SysPermission;
import csnight.redis.monitor.db.repos.SysMenuRepository;
import csnight.redis.monitor.db.repos.SysPermissionRepository;
import csnight.redis.monitor.db.repos.SysRoleRepository;
import csnight.redis.monitor.exception.ConflictsException;
import csnight.redis.monitor.rest.sys.dto.PermissionDto;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class PermissionServiceImpl {
    @Resource
    private SysRoleRepository sysRoleRepository;
    @Resource
    private SysPermissionRepository permissionRepository;
    @Resource
    private SysMenuRepository sysMenuRepository;

    /**
     * 功能描述: 搜索权限
     *
     * @param exp 查询条件
     * @return java.util.List<csnight.redis.monitor.db.jpa.SysPermission>
     * @author csnight
     * @since 2019-12-26 22:19
     */
    public List<SysPermission> QueryBy(PermitQueryExp exp) {
        return permissionRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryAnnotationProcess.getPredicate(root, exp, criteriaBuilder));
    }

    /**
     * 功能描述: 获取所有权限
     *
     * @return java.util.List<csnight.redis.monitor.db.jpa.SysPermission>
     * @author csnight
     * @since 2019-12-26 22:20
     */
    @Cacheable(value = "permits")
    public List<SysPermission> GetAllPermission() {
        return permissionRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    /**
     * 功能描述: 新建权限
     *
     * @param dto      权限信息
     * @param username 用户名
     * @return csnight.redis.monitor.db.jpa.SysPermission
     * @author csnight
     * @since 2019-12-26 22:21
     */
    @CacheEvict(value = "permits", beforeInvocation = true, allEntries = true)
    public SysPermission NewPermission(PermissionDto dto, String username) throws ConflictsException {
        SysPermission permission = new SysPermission();
        permission.setName(dto.getName());
        permission.setDescription(dto.getDescription());
        permission.setCreate_time(new Date());
        permission.setCreate_user(username);
        if (checkConflictPermit(permission, true) && MenuExist(dto.getMenu())) {
            permission.setMenu(dto.getMenu());
            return permissionRepository.save(permission);
        } else {
            throw new ConflictsException("Permission with same name already exists or menu belongs dosen't exists!");
        }
    }

    /**
     * 功能描述: 根据ID删除权限
     *
     * @param id 权限id
     * @return java.lang.String
     * @author csnight
     * @since 2019-12-26 22:21
     */
    @CacheEvict(value = "permits", beforeInvocation = true, allEntries = true)
    public String DeletePermitById(String id) {
        Optional<SysPermission> optPermit = permissionRepository.findById(id);
        if (optPermit.isPresent()) {
            //解绑权限与角色关联
            permissionRepository.untiedPermission(id);
            permissionRepository.deleteById(id);
            return "success";
        }
        return "failed";
    }

    /**
     * 功能描述: 修改权限
     *
     * @param dto 权限信息
     * @return csnight.redis.monitor.db.jpa.SysPermission
     * @author csnight
     * @since 2019-12-26 22:21
     */
    @CacheEvict(value = "permits", beforeInvocation = true, allEntries = true)
    public SysPermission ModifyPermission(PermissionDto dto) throws ConflictsException {
        Optional<SysPermission> optPermit = permissionRepository.findById(dto.getId());
        if (optPermit.isPresent()) {
            SysPermission old_permit = optPermit.get();
            if (checkConflictPermit(old_permit, false) && MenuExist(dto.getMenu())) {
                old_permit.setDescription(dto.getDescription());
                old_permit.setName(dto.getName());
                old_permit.setMenu(dto.getMenu());
                return permissionRepository.save(old_permit);
            } else {
                throw new ConflictsException("Department with same name already exists!");
            }
        }
        return null;
    }

    /**
     * 功能描述: 检查权限冲突
     *
     * @param permission 权限实体
     * @param isNew      是否新增
     * @return boolean
     * @author csnight
     * @since 2019-12-26 22:21
     */
    private boolean checkConflictPermit(SysPermission permission, boolean isNew) {
        boolean isValid = true;
        if (isNew) {
            SysPermission hasSame = permissionRepository.findByName(permission.getName());
            if (hasSame != null) {
                isValid = false;
            }
        } else {
            Optional<SysPermission> original = permissionRepository.findById(permission.getId());
            if (original.isPresent()) {
                SysPermission origin_permit = original.get();
                if (!origin_permit.getName().equals(permission.getName())) {
                    SysPermission hasSame = permissionRepository.findByName(permission.getName());
                    if (hasSame != null) {
                        isValid = false;
                    }
                }
            }
        }
        return isValid;
    }

    /**
     * 功能描述: 检查菜单可用性
     *
     * @param menu 菜单参数
     * @return boolean
     * @author csnight
     * @since 2019-12-26 22:21
     */
    private boolean MenuExist(SysMenu menu) {
        boolean isExist = true;
        Optional<SysMenu> exists = sysMenuRepository.findById(menu.getId());
        if (!exists.isPresent()) {
            isExist = false;
        }
        return isExist;
    }
}
