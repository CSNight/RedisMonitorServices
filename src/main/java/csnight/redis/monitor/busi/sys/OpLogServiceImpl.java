package csnight.redis.monitor.busi.sys;

import csnight.redis.monitor.busi.sys.exp.OpLogQueryExp;
import csnight.redis.monitor.db.blurry.QueryAnnotationProcess;
import csnight.redis.monitor.db.jpa.SysOpLog;
import csnight.redis.monitor.db.jpa.SysRole;
import csnight.redis.monitor.db.jpa.SysUser;
import csnight.redis.monitor.db.repos.SysLogRepository;
import csnight.redis.monitor.db.repos.SysUserRepository;
import csnight.redis.monitor.utils.BaseUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OpLogServiceImpl {
    @Resource
    private SysLogRepository sysLogRepository;
    @Resource
    private SysUserRepository userRepository;

    /**
     * 功能描述: 日志保存
     *
     * @param logs 日志实体
     * @author chens
     * @since 2019/12/26 10:34
     */
    public void SaveAll(List<SysOpLog> logs) {
        sysLogRepository.saveAll(logs);
    }

    /**
     * 功能描述: 分页日志查询
     *
     * @param username 用户名
     * @param cur      当前页
     * @param size     每页数量
     * @return : org.springframework.data.domain.Page<csnight.redis.monitor.db.jpa.SysOpLog>
     * @author chens
     * @since 2019/12/26 10:34
     */
    public Page<SysOpLog> GetLogsByUser(String username, int cur, int size) {
        Sort s = Sort.by(Sort.Direction.ASC, "ct");
        cur = cur - 1;
        Pageable pageable = PageRequest.of(cur, size, s);
        return sysLogRepository.findByUn(username, pageable);
    }

    /**
     * 功能描述: 日志条件查询
     *
     * @param exp 日志查询条件
     * @return : org.springframework.data.domain.Page<csnight.redis.monitor.db.jpa.SysOpLog>
     * @author chens
     * @since 2019/12/26 10:35
     */
    public Page<SysOpLog> QueryLogsByCond(OpLogQueryExp exp) {
        SysUser user = userRepository.findByUsername(exp.getUn());
        if (user == null) {
            String username = BaseUtils.GetUserFromContext();
            SysUser cur_user = userRepository.findByUsername(username);
            List<String> roles = cur_user.getRoles().stream().map(SysRole::getCode).collect(Collectors.toList());
            if (!roles.contains("ROLE_DEV") && !roles.contains("ROLE_SUPER")) {
                return null;
            }
        }
        Sort sort;
        if (exp.getSort() == null) {
            sort = Sort.by(Sort.Direction.ASC, "ct");
        } else {
            sort = Sort.by(Sort.Direction.fromString(exp.getDirect()), exp.getSort());
        }
        int cur = exp.getCur() - 1;
        int size = exp.getSize();
        if (cur < 0) {
            cur = 0;
        }
        if (exp.getSize() < 1) {
            size = 5;
        }
        Pageable pageable = PageRequest.of(cur, size, sort);
        return sysLogRepository.findAll((root, criteriaQuery, criteriaBuilder) ->
                QueryAnnotationProcess.getPredicate(root, exp, criteriaBuilder), pageable);

    }

    /**
     * 功能描述: 清空用户日志
     *
     * @param user 用户名
     * @return : java.lang.String
     * @author chens
     * @since 2019/12/26 10:35
     */
    public String ClearOpLog(String user) {
        if (user.equals("all")) {
            sysLogRepository.deleteAll();
            return "success";
        } else {
            SysUser sysUser = userRepository.findByUsername(user);
            if (sysUser != null) {
                sysLogRepository.deleteByUn(user);
                return "success";
            } else {
                return "User not found";
            }
        }
    }
}
