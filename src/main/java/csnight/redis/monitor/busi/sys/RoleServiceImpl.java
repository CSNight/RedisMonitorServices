package csnight.redis.monitor.busi.sys;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.busi.sys.exp.RoleQueryExp;
import csnight.redis.monitor.db.blurry.QueryAnnotationProcess;
import csnight.redis.monitor.db.jpa.SysCommands;
import csnight.redis.monitor.db.jpa.SysMenu;
import csnight.redis.monitor.db.jpa.SysRole;
import csnight.redis.monitor.db.jpa.SysUser;
import csnight.redis.monitor.db.repos.SysCommandRepository;
import csnight.redis.monitor.db.repos.SysRoleRepository;
import csnight.redis.monitor.db.repos.SysUserRepository;
import csnight.redis.monitor.exception.ConflictsException;
import csnight.redis.monitor.msg.series.RedisCmdType;
import csnight.redis.monitor.rest.sys.dto.RoleDto;
import csnight.redis.monitor.rest.sys.vo.SysMenuVo;
import csnight.redis.monitor.utils.BaseUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class RoleServiceImpl {
    @Resource
    private SysRoleRepository sysRoleRepository;
    @Resource
    private SysUserRepository userRepository;
    @Resource
    private SysCommandRepository commandRepository;

    public List<JSONObject> GetCommands() {
        List<JSONObject> JoCommands = new ArrayList<>();
        String[] commands = RedisCmdType.toCommands().split(",");
        for (String cmd : commands) {
            JSONObject joCmd = new JSONObject();
            joCmd.put("key", cmd);
            joCmd.put("label", cmd);
            JoCommands.add(joCmd);
        }
        return JoCommands;
    }

    /**
     * 功能描述: 角色查询
     *
     * @param exp 角色查询条件
     * @return java.util.List<csnight.redis.monitor.db.jpa.SysRole>
     * @author csnight
     * @since 2019-12-26 22:24
     */
    public List<SysRole> QueryBy(RoleQueryExp exp) {
        List<SysRole> roles = sysRoleRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryAnnotationProcess.getPredicate(root, exp, criteriaBuilder));
        //清除角色授权菜单关联查询的子菜单
        for (SysRole role : roles) {
            for (SysMenu menu : role.getMenus()) {
                menu.setChildren(new ArrayList<>());
            }
        }
        return roles;
    }

    /**
     * 功能描述: 获取全部角色
     *
     * @return java.util.List<csnight.redis.monitor.db.jpa.SysRole>
     * @author csnight
     * @since 2019-12-26 22:24
     */
    @Cacheable(value = "roles")
    public List<SysRole> GetAllRole() {
        List<SysRole> roles = sysRoleRepository.findAll(Sort.by(Sort.Direction.ASC, "level"));
        //清除角色授权菜单关联查询的子菜单
        for (SysRole role : roles) {
            for (SysMenu menu : role.getMenus()) {
                menu.setChildren(new ArrayList<>());
            }
        }
        return roles;
    }

    /**
     * 功能描述: 新增角色
     *
     * @param dto 角色信息
     * @return csnight.redis.monitor.db.jpa.SysRole
     * @author csnight
     * @since 2019-12-26 22:24
     */
    @CacheEvict(value = "roles", beforeInvocation = true, allEntries = true)
    public SysRole NewRole(RoleDto dto) throws ConflictsException {
        SysRole role = new SysRole();
        role.setName(dto.getName());
        role.setCode(dto.getCode());
        role.setCt(new Date());
        role.setLevel(dto.getLevel());
        role.setPermission(dto.getPermissionSet());
        if (checkConflictPermit(role, true)) {
            return sysRoleRepository.save(role);
        } else {
            throw new ConflictsException("Role with same name or code already exists!");
        }
    }

    /**
     * 功能描述: 根据id删除角色
     *
     * @param id 角色id
     * @return java.lang.String
     * @author csnight
     * @since 2019-12-26 22:24
     */
    @CacheEvict(value = "roles", beforeInvocation = true, allEntries = true)
    public String DeleteRoleById(String id) {
        SysRole role = sysRoleRepository.findOnly(id);
        if (role != null) {
            for (SysUser user : role.getUsers()) {
                user.setRoles(new HashSet<>());
                userRepository.save(user);
            }
            sysRoleRepository.deleteById(id);
            if (role.getCommands() != null) {
                commandRepository.delete(role.getCommands());
            }
            return "success";
        }
        return "failed";
    }

    /**
     * 功能描述: 修改角色信息
     *
     * @param dto 角色实例
     * @return csnight.redis.monitor.db.jpa.SysRole
     * @author csnight
     * @since 2019-12-26 22:24
     */
    @CacheEvict(value = "roles", beforeInvocation = true, allEntries = true)
    public SysRole ModifyRole(RoleDto dto) throws ConflictsException {
        SysRole old_role = sysRoleRepository.findOnly(dto.getId());
        if (old_role != null) {
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

    /**
     * 功能描述: 更新角色菜单授权
     *
     * @param dto 角色Dto
     * @return csnight.redis.monitor.db.jpa.SysRole
     * @author csnight
     * @since 2019-12-26 22:24
     */
    @CacheEvict(value = "roles", beforeInvocation = true, allEntries = true)
    public SysRole UpdateRoleMenus(RoleDto dto) {
        SysRole old_role = sysRoleRepository.findOnly(dto.getId());
        if (old_role != null) {
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

    /**
     * 功能描述: 更新角色权限
     *
     * @param dto 角色Dto
     * @return csnight.redis.monitor.db.jpa.SysRole
     * @author csnight
     * @since 2019-12-26 22:24
     */
    @CacheEvict(value = "roles", beforeInvocation = true, allEntries = true)
    public SysRole UpdateRolePermissions(RoleDto dto) {
        SysRole old_role = sysRoleRepository.findOnly(dto.getId());
        if (old_role != null) {
            old_role.setPermission(dto.getPermissionSet());
            SysRole sysRole = sysRoleRepository.save(old_role);
            for (SysMenu sysMenu : sysRole.getMenus()) {
                sysMenu.setChildren(new ArrayList<>());
            }
            return sysRole;
        }
        return null;
    }

    @CacheEvict(value = "roles", beforeInvocation = true, allEntries = true)
    public SysRole UpdateRoleCommands(RoleDto dto) {
        SysRole old_role = sysRoleRepository.findOnly(dto.getId());
        if (old_role != null) {
            SysCommands commands;
            if (old_role.getCommands() != null) {
                commands = old_role.getCommands();
                commands.setCommands(dto.getCommands().getCommands());
            } else {
                commands = dto.getCommands();
                commands.setCreate_time(new Date());
                commands.setCreate_user(BaseUtils.GetUserFromContext());
            }
            SysCommands sysCommands = commandRepository.save(commands);
            old_role.setCommands(sysCommands);
            return sysRoleRepository.save(old_role);
        }
        return null;
    }

    /**
     * 功能描述: 检查角色冲突
     *
     * @param old_role 原始角色
     * @param isNew    是否新增
     * @return boolean
     * @author csnight
     * @since 2019-12-26 22:24
     */
    private boolean checkConflictPermit(SysRole old_role, boolean isNew) {
        boolean isValid = true;
        //名称及代码均需检查
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
            SysRole origin_role = sysRoleRepository.findOnly(old_role.getId());
            if (origin_role != null) {
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
