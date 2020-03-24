package csnight.redis.monitor.busi.sys;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.auth.config.JdbcTokenRepositoryExt;
import csnight.redis.monitor.busi.rms.RmsDataDumpImpl;
import csnight.redis.monitor.busi.rms.RmsInsManageImpl;
import csnight.redis.monitor.busi.sys.exp.UserQueryExp;
import csnight.redis.monitor.db.blurry.QueryAnnotationProcess;
import csnight.redis.monitor.db.jpa.RmsInstance;
import csnight.redis.monitor.db.jpa.RmsShakeRecord;
import csnight.redis.monitor.db.jpa.SysOrg;
import csnight.redis.monitor.db.jpa.SysUser;
import csnight.redis.monitor.db.repos.RmsInsRepository;
import csnight.redis.monitor.db.repos.RmsShakeRepository;
import csnight.redis.monitor.db.repos.SysOrgRepository;
import csnight.redis.monitor.db.repos.SysUserRepository;
import csnight.redis.monitor.exception.ConflictsException;
import csnight.redis.monitor.rest.rms.dto.RecordsDto;
import csnight.redis.monitor.rest.sys.dto.UserEditDto;
import csnight.redis.monitor.rest.sys.dto.UserPassDto;
import csnight.redis.monitor.rest.sys.vo.UserVo;
import csnight.redis.monitor.utils.BaseUtils;
import csnight.redis.monitor.utils.ReflectUtils;
import csnight.redis.monitor.utils.RegexUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
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
    @Resource
    private SessionRegistry registry;
    @Resource
    private JdbcTokenRepositoryExt tokenRepositoryExt;
    @Resource
    private MailSendServiceImpl mailService;

    /**
     * 功能描述: 查询所有用户
     *
     * @return java.util.List<csnight.redis.monitor.db.jpa.SysUser>
     * @author csnight
     * @since 2019-12-26 22:31
     */
    @Cacheable(value = "users")
    public List<SysUser> GetAllUser() {
        List<SysUser> users = sysUserRepository.findAll();
        users.sort(new ComparatorUser());
        //清除用户头像及菜单信息,减小接口传输数据大小，避免json递归错误
        for (SysUser user : users) {
            user.setPassword("");
            user.getRoles().forEach(role -> {
                role.setMenus(new HashSet<>());
            });
            user.setHead_img(new byte[]{});
        }
        return users;
    }

    /**
     * 功能描述:
     *
     * @param exp 用户模糊查询条件
     * @return java.util.List<csnight.redis.monitor.db.jpa.SysUser>
     * @author csnight
     * @since 2019-12-26 22:31
     */
    public List<SysUser> QueryBy(UserQueryExp exp) {
        List<SysUser> users = sysUserRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryAnnotationProcess.getPredicate(root, exp, criteriaBuilder));
        //清除用户头像及菜单信息,减小接口传输数据大小，避免json递归错误
        for (SysUser user : users) {
            user.setPassword("");
            user.getRoles().forEach(role -> role.setMenus(new HashSet<>()));
            user.setHead_img(new byte[]{});
        }
        //用户排序
        users.sort(new ComparatorUser());
        return users;
    }

    /**
     * 功能描述: 根据部门id检索用户
     *
     * @param org_id 部门ID
     * @return java.util.List<csnight.redis.monitor.db.jpa.SysUser>
     * @author csnight
     * @since 2019-12-26 22:31
     */
    @CacheEvict(value = "users", beforeInvocation = true, allEntries = true)
    public List<SysUser> GetUsersByOrg(Long org_id) {
        List<SysUser> users = sysUserRepository.findByOrgId(org_id);
        SysOrg org = sysOrgRepository.findOnly(org_id);
        if (org != null) {
            Set<SysOrg> ids = new HashSet<>();
            getOrgChildIds(org, ids);
            if (org.getChildren().size() != 0) {
                for (SysOrg ch : ids) {
                    List<SysUser> user_ch = sysUserRepository.findByOrgId(ch.getId());
                    users.addAll(user_ch);
                }
            }
        }
        users.sort(new ComparatorUser());
        //清除用户头像及菜单信息,减小接口传输数据大小，避免json递归错误
        for (SysUser user : users) {
            user.setPassword("");
            user.setHead_img(new byte[]{});
        }
        return users;
    }

    /**
     * 功能描述: 查询部门所属子部门
     *
     * @param sysOrg 部门实体
     * @param ids    子部门列表
     * @return void
     * @author csnight
     * @since 2019-12-26 22:31
     */
    private void getOrgChildIds(SysOrg sysOrg, Set<SysOrg> ids) {
        for (SysOrg child : sysOrg.getChildren()) {
            ids.add(child);
            if (child.getChildren().size() > 0) {
                getOrgChildIds(child, ids);
            }
        }
    }

    /**
     * 功能描述: 新增用户
     *
     * @param dto 用户Dto
     * @return csnight.redis.monitor.rest.sys.vo.UserVo
     * @author csnight
     * @since 2019-12-26 22:31
     */
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

    /**
     * 功能描述: 修改用户信息
     *
     * @param dto 用户Dto
     * @return csnight.redis.monitor.rest.sys.vo.UserVo
     * @author csnight
     * @since 2019-12-26 22:31
     */
    @CacheEvict(value = {"user_info", "users"}, beforeInvocation = true, allEntries = true)
    public UserVo ModifyUser(UserEditDto dto) throws ConflictsException {
        SysUser user = sysUserRepository.findByUsername(dto.getUsername());
        if (user != null && CheckParams(dto, user, false)) {
            user.setUsername(dto.getUsername());
            user.setNick_name(dto.getNick_name());
            user.setEnabled(dto.isEnabled());
            if (!dto.isEnabled()) {
                CheckEnable(dto.getUsername());
            }
            user.setPhone(dto.getPhone());
            user.setEmail(dto.getEmail());
            user.setRoles(dto.getRoles());
            user.setOrg_id(dto.getOrg_id());
            return JSONObject.parseObject(JSONObject.toJSONString(sysUserRepository.save(user)), UserVo.class);
        }
        return null;
    }

    /**
     * 功能描述: 修改用户状态，若修改用户禁用，则踢出登录并清空该用户remember-me token存储
     *
     * @param username 用户名
     * @author csnight
     * @since 2019-12-26 22:31
     */
    private void CheckEnable(String username) {
        List<Object> userList = registry.getAllPrincipals();
        for (Object o : userList) {
            UserDetails userTemp = (UserDetails) o;
            if (userTemp.getUsername().equals(username)) {
                List<SessionInformation> sessionInformationList = registry.getAllSessions(userTemp, false);
                if (sessionInformationList != null) {
                    //session过期，踢出登录
                    for (SessionInformation sessionInformation : sessionInformationList) {
                        sessionInformation.expireNow();
                    }
                    //清空remember-me token
                    List<PersistentRememberMeToken> extTokenForName = tokenRepositoryExt.getTokenForName(username);
                    for (PersistentRememberMeToken persistentRememberMeToken : extTokenForName) {
                        String name = persistentRememberMeToken.getUsername();
                        String token = persistentRememberMeToken.getTokenValue();
                        tokenRepositoryExt.removeUserOldToken(name, token);
                    }
                }
            }
        }
    }

    /**
     * 功能描述: 检查新增或修改用户的信息是否符合条件
     *
     * @param dto   用户dto
     * @param user  原始用户
     * @param isNew 是否新增
     * @return boolean
     * @author csnight
     * @since 2019-12-26 22:31
     */
    private boolean CheckParams(UserEditDto dto, SysUser user, boolean isNew) throws ConflictsException {
        //用户名冲突检查
        if (!dto.getUsername().equals(user.getUsername()) || isNew) {
            if (sysUserRepository.findByUsername(dto.getUsername()) != null) {
                throw new ConflictsException("Username already exists!");
            }
        }
        //昵称冲突检查
        if (!dto.getNick_name().equals(user.getNick_name()) || isNew) {
            if (sysUserRepository.findByNickName(dto.getNick_name()) != null) {
                throw new ConflictsException("Nickname already exists!");
            }
        }
        //角色检查
        if (dto.getRoles().size() == 0) {
            throw new ConflictsException("Role must not be empty!");
        }
        //邮箱冲突检查
        if (!dto.getEmail().equals(user.getEmail()) || isNew) {
            if (!RegexUtils.checkEmail(dto.getEmail()) || sysUserRepository.findByEmail(dto.getEmail()) != null) {
                throw new ConflictsException("Email already exists or format wrong!");
            }
        }
        //手机号冲突检查
        if (!dto.getPhone().equals(user.getPhone()) || isNew) {
            if (!RegexUtils.checkPhone(dto.getPhone()) || sysUserRepository.findByPhone(dto.getPhone()) != null) {
                throw new ConflictsException("Phone already exists or format wrong!");
            }
        }
        return true;
    }

    /**
     * 功能描述: 修改密码
     *
     * @param user 用户密码Dto
     * @return java.lang.String
     * @author csnight
     * @since 2019-12-26 22:32
     */
    public String ChangePassword(UserPassDto user) {
        try {
            SysUser userExist = sysUserRepository.findByUsername(user.getUsername());
            if (userExist != null) {
                //原始密码检查
                boolean match = passwordEncoder.matches(user.getOld_password(), userExist.getPassword());
                if (match) {
                    //新密码加密存储
                    userExist.setPassword(passwordEncoder.encode(user.getPassword()));
                    sysUserRepository.save(userExist);
                    return "success";
                }
            }
            return "User name or password mismatch";
        } catch (Exception ex) {
            return "Password change failed";
        }
    }

    /**
     * 功能描述: 修改用户头像
     *
     * @param file     用户头像
     * @param username 用户名
     * @return java.lang.String
     * @author csnight
     * @since 2019-12-26 22:32
     */
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

    /**
     * 功能描述: 根据id 删除用户
     *
     * @param id 用户给id
     * @return java.lang.String
     * @author csnight
     * @since 2019-12-26 22:32
     */
    @CacheEvict(value = "users", beforeInvocation = true, allEntries = true)
    public String DeleteUserById(String id) {
        try {
            SysUser user = sysUserRepository.findOnly(id);
            if (user != null) {
                sysUserRepository.delete(user);
                ClearUserResource(user);
                return "success";
            }
        } catch (Exception e) {
            return "Delete cause exception";
        }
        return "failed";
    }

    /**
     * 功能描述:根据用户名删除用户
     *
     * @param username 用户名
     * @return java.lang.String
     * @author csnight
     * @since 2019-12-26 22:32
     */
    @CacheEvict(value = "user_info", key = "#username")
    public String DeleteUserByName(String username) {
        try {
            SysUser user = sysUserRepository.findByUsername(username);
            if (user != null) {
                sysUserRepository.delete(user);
                ClearUserResource(user);
                return "success";
            }
        } catch (Exception e) {
            return "Delete cause exception";
        }
        return "failed";
    }

    private void ClearUserResource(SysUser user) {
        //邮件清理
        mailService.DeleteUserMailResource(user.getUsername());
        //备份清理
        RmsShakeRepository shakeRepository = ReflectUtils.getBean(RmsShakeRepository.class);
        List<RmsShakeRecord> shakeRecords = shakeRepository.findByCreateUser(user.getUsername());
        RecordsDto dto = new RecordsDto();
        for (RmsShakeRecord record : shakeRecords) {
            dto.getIds().add(record.getId());
        }
        ReflectUtils.getBean(RmsDataDumpImpl.class).DeleteMultiRecords(dto);
        //清理实例
        RmsInsRepository insRepository = ReflectUtils.getBean(RmsInsRepository.class);
        List<RmsInstance> instances = insRepository.findByUserId(user.getId());
        RmsInsManageImpl insManage = ReflectUtils.getBean(RmsInsManageImpl.class);
        for (RmsInstance instance : instances) {
            insManage.DeleteInstance(instance.getId());
        }
    }

    /**
     * 功能描述: 根据用户角色级别排序
     *
     * @author csnight
     * @since 2019-12-26 22:32
     */
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
