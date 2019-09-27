package com.csnight.redis.monitor.quartz.jobs;

import com.csnight.redis.monitor.auth.handler.LoginFailureHandler;
import com.csnight.redis.monitor.auth.jpa.SysUser;
import com.csnight.redis.monitor.auth.repos.SysUserRepository;
import com.csnight.redis.monitor.utils.ReflectUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class Job_UnlockAccount implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        SysUserRepository sysUserRepository = ReflectUtils.getBean(SysUserRepository.class);
        LoginFailureHandler loginFailureHandler = ReflectUtils.getBean(LoginFailureHandler.class);
        String username = jobExecutionContext.getJobDetail().getJobDataMap().getString("params");
        try {
            SysUser sysUser = sysUserRepository.findByUsername(username);
            sysUser.setEnabled(true);
            sysUser.setLock_by("none");
            sysUserRepository.save(sysUser);
            System.out.println(username + ":解除锁定");
        } catch (Exception ignored) {

        } finally {
            loginFailureHandler.getLock_list().remove(username);
        }
    }
}
