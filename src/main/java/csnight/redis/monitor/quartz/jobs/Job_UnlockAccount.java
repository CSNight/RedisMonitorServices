package csnight.redis.monitor.quartz.jobs;

import csnight.redis.monitor.auth.handler.LoginFailureHandler;
import csnight.redis.monitor.db.jpa.SysUser;
import csnight.redis.monitor.db.repos.SysUserRepository;
import csnight.redis.monitor.utils.ReflectUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Job_UnlockAccount implements Job {
    private static Logger _log = LoggerFactory.getLogger(Job_UnlockAccount.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        SysUserRepository sysUserRepository = ReflectUtils.getBean(SysUserRepository.class);
        LoginFailureHandler loginFailureHandler = ReflectUtils.getBean(LoginFailureHandler.class);
        String username = jobExecutionContext.getJobDetail().getJobDataMap().getString("params");
        try {
            SysUser sysUser = sysUserRepository.findByUsername(username);
            sysUser.setEnabled(true);
            sysUser.setLock_by("none");
            sysUserRepository.save(sysUser);
            _log.info(username + ":解除锁定");
        } catch (Exception ignored) {
            _log.error(ignored.getMessage());
        } finally {
            loginFailureHandler.getLock_list().remove(username);
            try {
                jobExecutionContext.getScheduler().deleteJob(jobExecutionContext.getJobDetail().getKey());
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
            _log.info(username + ":锁定移除");
        }
    }
}
