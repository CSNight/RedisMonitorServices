package com.redis.monitor.busi.sys;

import com.redis.monitor.db.jpa.SysOrg;
import com.redis.monitor.db.jpa.SysPermission;
import com.redis.monitor.db.jpa.SysRole;
import com.redis.monitor.db.jpa.SysUser;
import com.redis.monitor.db.repos.SysOrgRepository;
import com.redis.monitor.db.repos.SysUserRepository;
import com.redis.monitor.utils.BaseUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class LoginUserService implements UserDetailsService {
    private final SysUserRepository userMapper;
    @Resource
    private SysOrgRepository orgRepository;

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

    @Cacheable(value = "user_info", key = "#username")
    public SysUser GetUserInfo(String username) {
        SysUser user = userMapper.findByUsername(username);
        Optional<SysOrg> org = orgRepository.findById(user.getOrg_id());
        if (org.isPresent()) {
            user.setPassword(org.get().getName());
        } else {
            user.setPassword("");
        }
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
