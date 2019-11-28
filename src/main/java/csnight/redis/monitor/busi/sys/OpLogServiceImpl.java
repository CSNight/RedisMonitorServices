package csnight.redis.monitor.busi.sys;

import csnight.redis.monitor.busi.sys.exp.OpLogQueryExp;
import csnight.redis.monitor.db.blurry.QueryAnnotationProcess;
import csnight.redis.monitor.db.jpa.SysOpLog;
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
import java.util.ArrayList;
import java.util.List;

@Service
public class OpLogServiceImpl {
    @Resource
    private SysLogRepository sysLogRepository;
    @Resource
    private SysUserRepository userRepository;

    public void SaveAll(List<SysOpLog> logs) {
        sysLogRepository.saveAll(logs);
    }

    public Page<SysOpLog> GetLogsByUser(String username, int cur, int size) {
        Sort s = Sort.by(Sort.Direction.ASC, "ct");
        cur = cur - 1;
        Pageable pageable = PageRequest.of(cur, size, s);
        return sysLogRepository.findByUn(username, pageable);
    }

    public Page<SysOpLog> QueryLogsByCond(OpLogQueryExp exp) {
        SysUser user = userRepository.findByUsername(exp.getUn());
        if (user == null) {
            String username = BaseUtils.GetUserFromContext();
            SysUser cur_user = userRepository.findByUsername(username);
            List<String> roles = new ArrayList<>();
            cur_user.getRoles().forEach(role -> roles.add(role.getCode()));
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