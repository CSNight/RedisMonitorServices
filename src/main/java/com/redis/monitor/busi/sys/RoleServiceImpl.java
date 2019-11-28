package com.redis.monitor.busi.sys;

import com.redis.monitor.busi.sys.exp.RoleQueryExp;
import com.redis.monitor.db.blurry.QueryAnnotationProcess;
import com.redis.monitor.db.jpa.SysMenu;
import com.redis.monitor.db.jpa.SysRole;
import com.redis.monitor.db.repos.SysRoleRepository;
import com.redis.monitor.exception.ConflictsException;
import com.redis.monitor.rest.sys.dto.RoleDto;
import com.redis.monitor.rest.sys.vo.SysMenuVo;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class RoleServiceImpl {
    @Resource
    private SysRoleRepository sysRoleRepository;

    public List<SysRole> QueryBy(RoleQueryExp exp) {
        List<SysRole> roles = sysRoleRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryAnnotationProcess.getPredicate(root, exp, criteriaBuilder));
        for (SysRole role : roles) {
            for (SysMenu menu : role.getMenus()) {
                menu.setChildren(new ArrayList<>());
            }
        }
        return roles;
    }

    @Cacheable(value = "roles")
    public List<SysRole> GetAllRole() {
        List<SysRole> roles = sysRoleRepository.findAll();
        for (SysRole role : roles) {
            for (SysMenu menu : role.getMenus()) {
                menu.setChildren(new ArrayList<>());
            }
        }
        return roles;
    }

    @CacheEvict(value = "roles", beforeInvocation = true, allEntries = true)
    public SysRole NewRole(RoleDto dto) throws ConflictsException {
        SysRole role = new SysRole();
        role.setName(dto.getName());
        role.setCode(dto.getCode());
        role.setCreate_time(new Date());
        role.setLevel(dto.getLevel());
        role.setPermission(dto.getPermissionSet());
        if (checkConflictPermit(role, true)) {
            return sysRoleRepository.save(role);
        } else {
            throw new ConflictsException("Role with same name or code already exists!");
        }
    }

    @CacheEvict(value = "roles", beforeInvocation = true, allEntries = true)
    public String DeleteRoleById(String id) {
        Optional<SysRole> optRole = sysRoleRepository.findById(id);
        if (optRole.isPresent()) {
            sysRoleRepository.deleteById(id);
            return "success";
        }
        return "failed";
    }

    @CacheEvict(value = "roles", beforeInvocation = true, allEntries = true)
    public SysRole ModifyRole(RoleDto dto) throws ConflictsException {
        Optional<SysRole> optRole = sysRoleRepository.findById(dto.getId());
        if (optRole.isPresent()) {
            SysRole old_role = optRole.get();
            if (checkConflictPermit(old_role, false)) {
                old_role.setCode(dto.getCode());
                old_role.setName(dto.getName());
                old_role.setLevel(dto.getLevel());
                return sysRoleRepository.save(old_role);
            } else {
                throw new ConflictsException("Role with same name or code already exists!");
            }
        }
        return null;
    }

    @CacheEvict(value = "roles", beforeInvocation = true, allEntries = true)
    public SysRole UpdateRoleMenus(RoleDto dto) {
        Optional<SysRole> optRole = sysRoleRepository.findById(dto.getId());
        if (optRole.isPresent()) {
            SysRole old_role = optRole.get();
            Set<SysMenu> menus = new HashSet<>();
            for (SysMenuVo menuVo : dto.getMenuSet()) {
                SysMenu sysMenu = new SysMenu();
                sysMenu.setId(menuVo.getId());
                menus.add(sysMenu);
            }
            old_role.setMenus(menus);
            SysRole sysRole = sysRoleRepository.save(old_role);
            for (SysMenu sysMenu : sysRole.getMenus()) {
                sysMenu.setChildren(new ArrayList<>());
            }
            return sysRole;
        }
        return null;
    }

    @CacheEvict(value = "roles", beforeInvocation = true, allEntries = true)
    public SysRole UpdateRolePermissions(RoleDto dto) {
        Optional<SysRole> optRole = sysRoleRepository.findById(dto.getId());
        if (optRole.isPresent()) {
            SysRole old_role = optRole.get();
            old_role.setPermission(dto.getPermissionSet());
            SysRole sysRole = sysRoleRepository.save(old_role);
            for (SysMenu sysMenu : sysRole.getMenus()) {
                sysMenu.setChildren(new ArrayList<>());
            }
            return sysRole;
        }
        return null;
    }

    private boolean checkConflictPermit(SysRole old_role, boolean isNew) {
        boolean isValid = true;
        if (isNew) {
            SysRole hasSameName = sysRoleRepository.findByName(old_role.getName());
            if (hasSameName != null) {
                isValid = false;
            }
            SysRole hasSameCode = sysRoleRepository.findByCode(old_role.getCode());
            if (hasSameCode != null) {
                isValid = false;
            }
        } else {
            Optional<SysRole> original = sysRoleRepository.findById(old_role.getId());
            if (original.isPresent()) {
                SysRole origin_role = original.get();
                if (!origin_role.getName().equals(old_role.getName())) {
                    SysRole hasSame = sysRoleRepository.findByName(old_role.getName());
                    if (hasSame != null) {
                        isValid = false;
                    }
                }
                if (!origin_role.getCode().equals(old_role.getCode())) {
                    SysRole hasSame = sysRoleRepository.findByCode(old_role.getCode());
                    if (hasSame != null) {
                        isValid = false;
                    }
                }
            }
        }
        return isValid;
    }
}
