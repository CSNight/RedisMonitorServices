package com.csnight.redis.monitor.busi.sys;

import com.alibaba.fastjson.JSONObject;
import com.csnight.redis.monitor.busi.sys.exp.UserQueryExp;
import com.csnight.redis.monitor.db.blurry.QueryAnnotationProcess;
import com.csnight.redis.monitor.db.jpa.SysOrg;
import com.csnight.redis.monitor.db.jpa.SysUser;
import com.csnight.redis.monitor.db.repos.SysOrgRepository;
import com.csnight.redis.monitor.db.repos.SysUserRepository;
import com.csnight.redis.monitor.exception.ConflictsException;
import com.csnight.redis.monitor.rest.sys.dto.UserEditDto;
import com.csnight.redis.monitor.rest.sys.dto.UserPassDto;
import com.csnight.redis.monitor.rest.sys.vo.UserVo;
import com.csnight.redis.monitor.utils.BaseUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.*;

@Service
public class UserServiceImpl {

    @Resource
    private SysUserRepository sysUserRepository;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private SysOrgRepository sysOrgRepository;

    @Cacheable(value = "users")
    public List<SysUser> GetAllUser() {
        List<SysUser> users = sysUserRepository.findAll();
        users.sort(new ComparatorUser());
        for (SysUser user : users) {
            user.setPassword("");
            user.getRoles().forEach(role -> {
                role.setMenus(new HashSet<>());
            });
            user.setHead_img(new byte[]{});
        }
        return users;
    }

    public List<SysUser> QueryBy(UserQueryExp exp) {
        List<SysUser> users = sysUserRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryAnnotationProcess.getPredicate(root, exp, criteriaBuilder));
        for (SysUser user : users) {
            user.setPassword("");
            user.getRoles().forEach(role -> role.setMenus(new HashSet<>()));
            user.setHead_img(new byte[]{});
        }
        users.sort(new ComparatorUser());
        return users;
    }

    @CacheEvict(value = "users", beforeInvocation = true, allEntries = true)
    public List<SysUser> GetUsersByOrg(Long org_id) {
        List<SysUser> users = sysUserRepository.findByOrgId(org_id);
        Optional<SysOrg> orgOpt = sysOrgRepository.findById(org_id);
        if (orgOpt.isPresent()) {
            Set<SysOrg> ids = new HashSet<>();
            SysOrg org = orgOpt.get();
            getOrgChildIds(org, ids);
            if (org.getChildren().size() != 0) {
                for (SysOrg ch : ids) {
                    List<SysUser> user_ch = sysUserRepository.findByOrgId(ch.getId());
                    users.addAll(user_ch);
                }
            }
        }
        users.sort(new ComparatorUser());
        for (SysUser user : users) {
            user.setPassword("");
            user.setHead_img(new byte[]{});
        }
        return users;
    }

    private void getOrgChildIds(SysOrg sysOrg, Set<SysOrg> ids) {
        for (SysOrg child : sysOrg.getChildren()) {
            ids.add(child);
            if (child.getChildren().size() > 0) {
                getOrgChildIds(child, ids);
            }
        }
    }

    @CacheEvict(value = "users", beforeInvocation = true, allEntries = true)
    public UserVo NewUsr(UserEditDto dto) throws ConflictsException {
        SysUser sysUser = new SysUser();
        if (CheckParams(dto, sysUser, true)) {
            sysUser.setUsername(dto.getUsername());
            sysUser.setNick_name(dto.getNick_name());
            sysUser.setEnabled(dto.isEnabled());
            sysUser.setPassword(passwordEncoder.encode("123456"));
            sysUser.setPhone(dto.getPhone());
            sysUser.setEmail(dto.getEmail());
            sysUser.setRoles(dto.getRoles());
            sysUser.setOrg_id(dto.getOrg_id());
            sysUser.setLock_by("none");
            sysUser.setCreate_time(new Date());
            SysUser user = sysUserRepository.save(sysUser);
            return JSONObject.parseObject(JSONObject.toJSONString(user), UserVo.class);
        }
        return null;
    }

    @CacheEvict(value = {"user_info", "users"}, beforeInvocation = true, allEntries = true)
    public UserVo ModifyUser(UserEditDto dto) throws ConflictsException {
        SysUser user = sysUserRepository.findByUsername(dto.getUsername());
        if (user != null && CheckParams(dto, user, false)) {
            user.setUsername(dto.getUsername());
            user.setNick_name(dto.getNick_name());
            user.setEnabled(dto.isEnabled());
            user.setPhone(dto.getPhone());
            user.setEmail(dto.getEmail());
            user.setRoles(dto.getRoles());
            user.setOrg_id(dto.getOrg_id());
            return JSONObject.parseObject(JSONObject.toJSONString(sysUserRepository.save(user)), UserVo.class);
        }
        return null;
    }

    private boolean CheckParams(UserEditDto dto, SysUser user, boolean isNew) throws ConflictsException {
        if (!dto.getUsername().equals(user.getUsername()) || isNew) {
            if (sysUserRepository.findByUsername(dto.getUsername()) != null) {
                throw new ConflictsException("Username already exists!");
            }
        }
        if (!dto.getNick_name().equals(user.getNick_name()) || isNew) {
            if (sysUserRepository.findByNickName(dto.getNick_name()) != null) {
                throw new ConflictsException("Nickname already exists!");
            }
        }
        if (dto.getRoles().size() == 0) {
            throw new ConflictsException("Role must not be empty!");
        }
        if (!dto.getEmail().equals(user.getEmail()) || isNew) {
            if (!BaseUtils.checkEmail(dto.getEmail()) || sysUserRepository.findByEmail(dto.getEmail()) != null) {
                throw new ConflictsException("Email already exists or format wrong!");
            }
        }
        if (!dto.getPhone().equals(user.getPhone()) || isNew) {
            if (!BaseUtils.checkPhone(dto.getPhone()) || sysUserRepository.findByPhone(dto.getPhone()) != null) {
                throw new ConflictsException("Phone already exists or format wrong!");
            }
        }
        return true;
    }

    public String ChangePassword(UserPassDto user) {
        try {
            SysUser userExist = sysUserRepository.findByUsername(user.getUsername());
            if (userExist != null) {
                boolean match = passwordEncoder.matches(user.getOld_password(), userExist.getPassword());
                if (match) {
                    userExist.setPassword(passwordEncoder.encode(user.getPassword()));
                    SysUser sysUser = sysUserRepository.save(userExist);
                    if (sysUser != null) {
                        return "success";
                    }
                }
            }
            return "User name or password mismatch";
        } catch (Exception ex) {
            return "Password change failed";
        }
    }

    @CacheEvict(value = "user_info", key = "#username")
    public String changeAvatar(MultipartFile file, String username) {
        try {
            String fn = file.getOriginalFilename();
            assert fn != null;
            String ext = fn.substring(fn.lastIndexOf(".") + 1);
            byte[] fb = file.getBytes();
            SysUser user = sysUserRepository.findByUsername(username);
            if (user != null && fb.length != 0) {
                user.setHead_img(BaseUtils.bytesToBase64(fb, ext).getBytes());
                sysUserRepository.save(user);
                System.gc();
                return "success";
            }
            return "failed";
        } catch (Exception ex) {
            return "failed";
        }
    }

    @CacheEvict(value = "users", beforeInvocation = true, allEntries = true)
    public String DeleteUserById(String id) {
        //TODO 删除用户资源
        try {
            Optional<SysUser> userOpt = sysUserRepository.findById(id);
            if (userOpt.isPresent()) {
                sysUserRepository.delete(userOpt.get());
                return "success";
            }
        } catch (Exception e) {
            return "Delete cause exception";
        }
        return "failed";
    }

    @CacheEvict(value = "user_info", key = "#username")
    public String DeleteUserByName(String username) {
        //TODO 删除用户资源
        try {
            SysUser user = sysUserRepository.findByUsername(username);
            if (user != null) {
                sysUserRepository.delete(user);
                return "success";
            }
        } catch (Exception e) {
            return "Delete cause exception";
        }
        return "failed";
    }

    private static class ComparatorUser implements Comparator<SysUser> {
        @Override
        public int compare(SysUser t0, SysUser t1) {
            List<Integer> t0_levels = new ArrayList<>();
            t0.getRoles().forEach(r -> t0_levels.add(r.getLevel()));
            List<Integer> t1_levels = new ArrayList<>();
            t1.getRoles().forEach(r -> t1_levels.add(r.getLevel()));
            if (t0_levels.size() == 0 || t1_levels.size() == 0) {
                return t0_levels.size() == 0 ? 1 : -1;
            }
            int t0l = Collections.min(t0_levels);
            int t1l = Collections.min(t1_levels);
            if (t0l == t1l) {
                return 0;
            }
            return t0l > t1l ? 1 : -1;
        }
    }
}
