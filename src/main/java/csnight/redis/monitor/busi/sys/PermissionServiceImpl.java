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

    public List<SysPermission> QueryBy(PermitQueryExp exp) {
        return permissionRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryAnnotationProcess.getPredicate(root, exp, criteriaBuilder));
    }

    @Cacheable(value = "permits")
    public List<SysPermission> GetAllPermission() {
        return permissionRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

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

    @CacheEvict(value = "permits", beforeInvocation = true, allEntries = true)
    public String DeletePermitById(String id) {
        Optional<SysPermission> optPermit = permissionRepository.findById(id);
        if (optPermit.isPresent()) {
            permissionRepository.untiedPermission(id);
            permissionRepository.deleteById(id);
            return "success";
        }
        return "failed";
    }

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

    private boolean MenuExist(SysMenu menu) {
        boolean isExist = true;
        Optional<SysMenu> exists = sysMenuRepository.findById(menu.getId());
        if (!exists.isPresent()) {
            isExist = false;
        }
        return isExist;
    }
}
