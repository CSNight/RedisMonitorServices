package csnight.redis.monitor.busi.sys;

import csnight.redis.monitor.db.jpa.SysUser;
import csnight.redis.monitor.db.repos.SysRoleRepository;
import csnight.redis.monitor.db.repos.SysUserRepository;
import csnight.redis.monitor.rest.sys.dto.UserSignDto;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;


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

    public void registerNewAccount(UserSignDto userSignDto) {
        SysUser sysUser = new SysUser();
        sysUser.setUsername(userSignDto.getUsername());
        sysUser.setEmail(userSignDto.getEmail());
        sysUser.setPhone(userSignDto.getPhone());
        sysUser.setNick_name(userSignDto.getUsername());
        sysUser.setEnabled(true);
        sysUser.setLogin_times(0);
        sysUser.setOrg_id(1L);
        sysUser.setPassword(passwordEncoder.encode(userSignDto.getPassword()));
        sysUser.setRoles(new HashSet<>(Collections.singletonList(sysRoleRepository.findByCode("ROLE_USER"))));
        sysUser.setCreate_time(new Date());
        sysUser.setLock_by("none");
        sysUser.setLast_login(new Date());
        save(sysUser);
    }
}