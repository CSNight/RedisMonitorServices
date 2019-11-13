package com.csnight.redis.monitor.busi;

import com.csnight.redis.monitor.aop.QueryAnnotationProcess;
import com.csnight.redis.monitor.busi.exp.PermitQueryExp;
import com.csnight.redis.monitor.db.jpa.SysMenu;
import com.csnight.redis.monitor.db.jpa.SysPermission;
import com.csnight.redis.monitor.db.repos.SysMenuRepository;
import com.csnight.redis.monitor.db.repos.SysPermissionRepository;
import com.csnight.redis.monitor.rest.dto.PermissionDto;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class PermissionServiceImpl {
    @Resource
    private SysPermissionRepository permissionRepository;
    @Resource
    private SysMenuRepository sysMenuRepository;

    public List<SysPermission> QueryBy(PermitQueryExp exp) {
        return permissionRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryAnnotationProcess.getPredicate(root, exp, criteriaBuilder));
    }

    public List<SysPermission> GetAllPermission() {
        return permissionRepository.findAll();
    }

    public SysPermission NewPermission(PermissionDto dto, String username) {
        SysPermission permission = new SysPermission();
        permission.setName(dto.getName());
        permission.setDescription(dto.getDescription());
        permission.setCreate_time(new Date());
        permission.setCreate_user(username);
        if (checkConflictPermit(permission, true) && MenuExist(dto.getMenu())) {
            permission.setMenu(dto.getMenu());
            return permissionRepository.save(permission);
        }
        return null;
    }

    public String DeletePermitById(String id) {
        Optional<SysPermission> optPermit = permissionRepository.findById(id);
        if (optPermit.isPresent()) {
            permissionRepository.deleteById(id);
            return "success";
        }
        return "failed";
    }

    public SysPermission ModifyPermission(PermissionDto dto) {
        Optional<SysPermission> optPermit = permissionRepository.findById(dto.getId());
        if (optPermit.isPresent()) {
            SysPermission old_permit = optPermit.get();
            if (checkConflictPermit(old_permit, false) && MenuExist(dto.getMenu())) {
                old_permit.setDescription(dto.getDescription());
                old_permit.setName(dto.getName());
                old_permit.setMenu(dto.getMenu());
                return permissionRepository.save(old_permit);
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
