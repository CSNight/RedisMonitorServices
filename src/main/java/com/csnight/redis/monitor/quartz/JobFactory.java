package com.csnight.redis.monitor.quartz;

import com.csnight.redis.monitor.quartz.config.*;
import com.csnight.redis.monitor.utils.JSONUtil;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@Component
public class JobFactory {
    private static Logger _log = LoggerFactory.getLogger(JobFactory.class);
    private final Scheduler scheduler;

    public JobFactory(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    private JobDetail JobDetailBuilder(Class<? extends Job> jobClass, JobConfig jc) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("params", jc.getInvokeParam());
        return JobBuilder.newJob(jobClass)
                .withDescription(jc.getDescription()) //job的描述
                .withIdentity(jc.getJobName(), jc.getJobGroup()).setJobData(jobDataMap)
                .build();
    }

    /**
     * 新建一个任务
     */
    public String AddJob(JobConfig jobConfig, Class<? extends Job> job) {
        try {
            BaseTriggerConfig baseTriggerConfig = getTriggerConfig(jobConfig);
            JobDetail jobDetail = JobDetailBuilder(job, jobConfig);
            Trigger trigger = baseTriggerConfig.getTrigger();
            scheduler.scheduleJob(jobDetail, trigger);
            return "success";
        } catch (Exception ex) {
            _log.error(ex.getMessage());
            return "failed";
        }

    }

    public String GetJobState(String jobName, String jobGroup) {
        try {
            TriggerKey triggerKey = new TriggerKey(jobName, jobGroup);
            return scheduler.getTriggerState(triggerKey).name();
        } catch (Exception ex) {
            _log.error(ex.getMessage());
            return "failed";
        }
    }

    //暂停所有任务
    public String PauseAllJob() {
        try {
            scheduler.pauseAll();
            return "success";
        } catch (Exception ex) {
            _log.error(ex.getMessage());
            return "failed";
        }
    }

    //暂停任务
    public String PauseJob(String jobName, String jobGroup) {
        try {
            JobKey jobKey = new JobKey(jobName, jobGroup);
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            if (jobDetail == null) {
                return "fail";
            } else {
                scheduler.pauseJob(jobKey);
                return "success";
            }
        } catch (Exception ex) {
            _log.error(ex.getMessage());
            return "failed";
        }
    }

    //恢复所有任务
    public String ResumeAllJob() {
        try {
            scheduler.resumeAll();
            return "success";
        } catch (Exception ex) {
            _log.error(ex.getMessage());
            return "failed";
        }
    }

    // 恢复某个任务
    public String ResumeJob(String jobName, String jobGroup) {
        try {
            JobKey jobKey = new JobKey(jobName, jobGroup);
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            if (jobDetail == null) {
                return "fail";
            } else {
                scheduler.resumeJob(jobKey);
                return "success";
            }
        } catch (Exception ex) {
            _log.error(ex.getMessage());
            return "failed";
        }
    }

    //删除某个任务
    public String DeleteJob(JobConfig jobConfigBase) throws SchedulerException {
        try {
            JobKey jobKey = new JobKey(jobConfigBase.getJobName(), jobConfigBase.getJobGroup());
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            if (jobDetail == null) {
                return "jobDetail is null";
            } else if (!scheduler.checkExists(jobKey)) {
                return "jobKey is not exists";
            } else {
                scheduler.deleteJob(jobKey);
                return "success";
            }
        } catch (Exception ex) {
            _log.error(ex.getMessage());
            return "failed";
        }

    }

    public String DeleteAllJob() {
        try {
            // 获取有所的组
            List<String> jobGroupNameList = scheduler.getJobGroupNames();
            for (String jobGroupName : jobGroupNameList) {
                GroupMatcher<JobKey> jobKeyGroupMatcher = GroupMatcher.jobGroupEquals(jobGroupName);
                Set<JobKey> jobKeySet = scheduler.getJobKeys(jobKeyGroupMatcher);
                for (JobKey jobKey : jobKeySet) {
                    String jobName = jobKey.getName();
                    JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                    if (jobDetail == null)
                        continue;
                    scheduler.deleteJob(jobKey);
                }
            }
            return "success";
        } catch (Exception e) {
            _log.error(e.getMessage());
            return "failed";
        }
    }

    //修改任务
    public String ModifyJob(JobConfig jobConfig) throws SchedulerException {
        try {
            BaseTriggerConfig baseTriggerConfig = getTriggerConfig(jobConfig);
            TriggerKey triggerKey = TriggerKey.triggerKey(jobConfig.getJobName(), jobConfig.getJobGroup());
            JobKey jobKey = new JobKey(jobConfig.getJobName(), jobConfig.getJobGroup());
            if (scheduler.checkExists(jobKey) && scheduler.checkExists(triggerKey)) {
                Trigger trigger = baseTriggerConfig.getTrigger();
                trigger.getJobDataMap().put("params", jobConfig.getInvokeParam());
                scheduler.rescheduleJob(triggerKey, trigger);
                return "success";
            } else {
                return "job or trigger not exists";
            }
        } catch (Exception e) {
            _log.error(e.getMessage());
            return "failed";
        }
    }

    private BaseTriggerConfig getTriggerConfig(JobConfig jobConfig) {
        BaseTriggerConfig baseTriggerConfig;
        switch (jobConfig.getTriggerType()) {
            default:
            case 0:
                baseTriggerConfig = JSONUtil.json2pojo(jobConfig.getTriggerConfig(), SimpleBaseTriggerConfig.class);
                break;
            case 1:
                baseTriggerConfig = JSONUtil.json2pojo(jobConfig.getTriggerConfig(), CronTriggerConfig.class);
                break;
            case 2:
                baseTriggerConfig = JSONUtil.json2pojo(jobConfig.getTriggerConfig(), DailyTimeIntervalBaseTriggerConfig.class);
                break;
            case 3:
                baseTriggerConfig = JSONUtil.json2pojo(jobConfig.getTriggerConfig(), CalendarIntervalBaseTriggerConfig.class);
                break;
        }
        return baseTriggerConfig;
    }
}
