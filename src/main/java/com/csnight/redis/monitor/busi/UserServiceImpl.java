package com.csnight.redis.monitor.busi;

import com.alibaba.fastjson.JSONObject;
import com.csnight.redis.monitor.db.jpa.SysUser;
import com.csnight.redis.monitor.db.repos.SysUserRepository;
import com.csnight.redis.monitor.exception.ConflictsException;
import com.csnight.redis.monitor.rest.dto.UserEditDto;
import com.csnight.redis.monitor.rest.dto.UserPassDto;
import com.csnight.redis.monitor.rest.vo.UserVo;
import com.csnight.redis.monitor.utils.BaseUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl {

    @Resource
    private SysUserRepository sysUserRepository;
    @Resource
    private PasswordEncoder passwordEncoder;

    public List<SysUser> GetAllUser() {
        List<SysUser> users = sysUserRepository.findAll();
        for (SysUser user : users) {
            user.setPassword("");
            user.setHead_img(new byte[]{});
        }
        return users;
    }

    public List<SysUser> GetUsersByOrg(Long org_id) {
        List<SysUser> users = sysUserRepository.findByOrgId(org_id);
        for (SysUser user : users) {
            user.setPassword("");
            user.setHead_img(new byte[]{});
        }
        return users;
    }

    public UserVo ModifyUser(UserEditDto dto) throws ConflictsException {
        SysUser user = sysUserRepository.findByUsername(dto.getUsername());
        if (user != null && CheckParams(dto, user)) {
            user.setUsername(dto.getUsername());
            user.setNick_name(dto.getNick_name());
            user.setEnabled(dto.isEnable());
            user.setPhone(dto.getPhone());
            user.setEmail(dto.getEmail());
            user.setRoles(dto.getRoles());
            user.setOrg_id(dto.getOrg_id());
            return JSONObject.parseObject(JSONObject.toJSONString(sysUserRepository.save(user)), UserVo.class);
        }
        return null;
    }

    private boolean CheckParams(UserEditDto dto, SysUser user) throws ConflictsException {
        if (!dto.getUsername().equals(user.getUsername())) {
            if (sysUserRepository.findByUsername(dto.getUsername()) != null) {
                throw new ConflictsException("Username already exists!");
            }
        }
        if (!dto.getNick_name().equals(user.getNick_name())) {
            if (sysUserRepository.findByNickName(dto.getNick_name()) != null) {
                throw new ConflictsException("Nickname already exists!");
            }
        }
        if (dto.getRoles().size() == 0) {
            throw new ConflictsException("Role must not be empty!");
        }
        if (!dto.getEmail().equals(user.getEmail())) {
            if (!BaseUtils.checkEmail(dto.getEmail()) || sysUserRepository.findByEmail(dto.getEmail()) != null) {
                throw new ConflictsException("Email already exists or format wrong!");
            }
        }
        if (!dto.getPhone().equals(user.getPhone())) {
            if (!BaseUtils.checkPhone(dto.getPhone()) || sysUserRepository.findByPhone(dto.getPhone()) != null) {
                throw new ConflictsException("Phone already exists or format wrong!");
            }
        }
        return true;
    }

    public String ChangePassword(UserPassDto user) {
        try {
            String passEncode = passwordEncoder.encode(user.getOld_password());
            SysUser userExist = sysUserRepository.findByUsernameAndPassword(user.getUsername(), passEncode);
            if (userExist != null) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                SysUser sysUser = sysUserRepository.save(userExist);
                if (sysUser != null) {
                    return "success";
                }
            }
            return "User name or password mismatch";
        } catch (Exception ex) {
            return "Password change failed";
        }
    }

    public String DeleteUserById(String id) {
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

    public String DeleteUserByName(String username) {
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
}
