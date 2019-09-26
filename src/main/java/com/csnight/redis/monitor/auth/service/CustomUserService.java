package com.csnight.redis.monitor.auth.service;

import com.csnight.redis.monitor.auth.jpa.SysPermission;
import com.csnight.redis.monitor.auth.jpa.SysRole;
import com.csnight.redis.monitor.auth.jpa.SysUser;
import com.csnight.redis.monitor.auth.repos.SysUserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CustomUserService implements UserDetailsService {
    private final SysUserRepository userMapper;

    public CustomUserService(SysUserRepository userMapper) {
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

    private SysUser checkByIdentify(String identify) {
        if (identify.contains("@") && checkEmail(identify)) {
            return userMapper.findByUsernameOrEmail(identify, identify);
        } else if (checkPhone(identify)) {
            return userMapper.findByPhone(identify);
        } else {
            return userMapper.findByUsername(identify);
        }
    }

    private List<GrantedAuthority> createAuthorities(List<SysRole> roles) {
        List<GrantedAuthority> auths = new ArrayList<>();
        for (SysRole role : roles) {
            for (SysPermission permission : role.getPermission())
                auths.add(new SimpleGrantedAuthority(permission.getName()));
        }
        return auths;
    }

    private boolean checkPhone(String identify) {
        String regex = "^[1][3,4,5,7,8,9][0-9]{9}$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(identify);
        return m.matches();
    }

    private boolean checkEmail(String identify) {
        String regex = "^[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(identify);
        return m.matches();
    }

}
