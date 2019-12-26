package csnight.redis.monitor.busi.sys;

import csnight.redis.monitor.db.jpa.SysOrg;
import csnight.redis.monitor.db.jpa.SysPermission;
import csnight.redis.monitor.db.jpa.SysRole;
import csnight.redis.monitor.db.jpa.SysUser;
import csnight.redis.monitor.db.repos.SysOrgRepository;
import csnight.redis.monitor.db.repos.SysUserRepository;
import csnight.redis.monitor.utils.RegexUtils;
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

    /**
     * 功能描述: 用户登录检查
     *
     * @param username 用户名
     * @return : org.springframework.security.core.userdetails.UserDetails
     * @author chens
     * @since 2019/12/26 10:32
     */
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

    /**
     * 功能描述: 用户信息插叙
     *
     * @param username 用户名
     * @return : csnight.redis.monitor.db.jpa.SysUser
     * @author chens
     * @since 2019/12/26 10:32
     */
    @Cacheable(value = "user_info", key = "#username")
    public SysUser GetUserInfo(String username) {
        SysUser user = userMapper.findByUsername(username);
        Optional<SysOrg> org = orgRepository.findById(user.getOrg_id());
        //利用密码字段传输部门信息，密码不应返回前端
        if (org.isPresent()) {
            user.setPassword(org.get().getName());
        } else {
            user.setPassword("");
        }
        return user;
    }

    /**
     * 功能描述:检查登录用户名类型
     *
     * @param identify
     * @return : csnight.redis.monitor.db.jpa.SysUser
     * @author chens
     * @since 2019/12/26 10:33
     */
    private SysUser checkByIdentify(String identify) {
        if (identify.contains("@") && RegexUtils.checkEmail(identify)) {
            return userMapper.findByUsernameOrEmail(identify, identify);
        } else if (RegexUtils.checkPhone(identify)) {
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
