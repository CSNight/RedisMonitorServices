package com.csnight.redis.monitor.auth.service;

import com.csnight.redis.monitor.auth.jpa.SysUser;
import com.csnight.redis.monitor.auth.jpa.UserDto;
import com.csnight.redis.monitor.auth.repos.SysRoleRepository;
import com.csnight.redis.monitor.auth.repos.SysUserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;


@Service
public class SignUpUserService {

    private SysRoleRepository sysRoleRepository;
    private SysUserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    public SignUpUserService(SysUserRepository userRepository, SysRoleRepository sysRoleRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.sysRoleRepository = sysRoleRepository;
    }

    public SysUser save(SysUser user) {
        return userRepository.save(user);
    }


    public SysUser findByEmail(String email) {
        return userRepository.findByEmail(email);
    }


    public SysUser findByName(String name) {
        return userRepository.findByUsername(name);
    }


    public boolean checkUserByName(String name) {
        SysUser user = findByName(name);
        return user != null;
    }

    public boolean checkUserByEmail(String email) {
        SysUser user = findByEmail(email);
        return user != null;
    }

    public SysUser registerNewAccount(UserDto userDto) {
        SysUser sysUser = new SysUser();
        sysUser.setUsername(userDto.getUsername());
        sysUser.setEmail(userDto.getEmail());
        sysUser.setNick_name(userDto.getUsername());
        sysUser.setEnabled(true);
        sysUser.setLogin_times(0);
        sysUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
        sysUser.setRoles(Collections.singletonList(sysRoleRepository.findByName("ROLE_ADMIN")));
        sysUser.setCreate_time(new Date());
        sysUser.setLast_login(new Date());
        return save(sysUser);
    }
}