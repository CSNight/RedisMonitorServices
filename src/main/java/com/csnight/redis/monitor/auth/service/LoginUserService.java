package com.csnight.redis.monitor.auth.service;

import com.csnight.redis.monitor.db.jpa.SysPermission;
import com.csnight.redis.monitor.db.jpa.SysRole;
import com.csnight.redis.monitor.db.jpa.SysUser;
import com.csnight.redis.monitor.db.repos.SysUserRepository;
import com.csnight.redis.monitor.utils.BaseUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class LoginUserService implements UserDetailsService {
    private final SysUserRepository userMapper;

    public LoginUserService(SysUserRepository userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = checkByIdentify(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        List<GrantedAuthority> simpleGrantedAuthorities = createAuthorities(user.getRoles());
        return new User(user.getUsername(), user.getPassword(), user.isEnabled(), user.isAccountNonExpired(),
                user.isCredentialsNonExpired(), user.isAccountNonLocked(), simpleGrantedAuthorities);
    }

    public SysUser GetUserInfo(String username) {
        SysUser user = userMapper.findByUsername(username);
        user.setPassword("");
        return user;
    }

    private SysUser checkByIdentify(String identify) {
        if (identify.contains("@") && BaseUtils.checkEmail(identify)) {
            return userMapper.findByUsernameOrEmail(identify, identify);
        } else if (BaseUtils.checkPhone(identify)) {
            return userMapper.findByPhone(identify);
        } else {
            return userMapper.findByUsername(identify);
        }
    }

    private List<GrantedAuthority> createAuthorities(Set<SysRole> roles) {
        List<GrantedAuthority> auths = new ArrayList<>();
        for (SysRole role : roles) {
            for (SysPermission permission : role.getPermission())
                auths.add(new SimpleGrantedAuthority(permission.getName()));
        }
        return auths;
    }


}
