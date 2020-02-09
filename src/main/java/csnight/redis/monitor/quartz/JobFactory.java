package csnight.redis.monitor.quartz;

import csnight.redis.monitor.quartz.config.*;
import csnight.redis.monitor.utils.JSONUtils;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
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

    public boolean ExistsJob(String jobName, String jobGroup) {
        JobKey jobKey = new JobKey(jobName, jobGroup);
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
        try {
            return scheduler.checkExists(jobKey) && scheduler.checkExists(triggerKey);
        } catch (SchedulerException e) {
            return false;
        }
    }

    public Object GetJobData(String jobName, String jobGroup) {
        JobKey jobKey = new JobKey(jobName, jobGroup);
        try {
            return scheduler.getJobDetail(jobKey).getJobDataMap().get("params");
        } catch (Exception e) {
            return null;
        }
    }

    public boolean SetJobData(String jobName, String jobGroup, Map<String, String> data) {
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
        JobKey jobKey = new JobKey(jobName, jobGroup);
        try {
            if (scheduler.checkExists(jobKey) && scheduler.checkExists(triggerKey)) {
                Trigger trigger = scheduler.getTrigger(triggerKey);
                trigger.getJobDataMap().put("params", data);
                JobDetail detail = scheduler.getJobDetail(jobKey);
                detail.getJobDataMap().put("params", data);
                String state = GetJobState(jobName, jobGroup);
                scheduler.pauseJob(jobKey);
                scheduler.pauseTrigger(triggerKey);
                scheduler.unscheduleJob(triggerKey);
                scheduler.scheduleJob(detail, trigger);
                if (state.equals("NORMAL")) {
                    scheduler.resumeJob(jobKey);
                }
                return true;
            } else {
                return false;
            }
        } catch (SchedulerException e) {
            return false;
        }
    }

    public String GetJobState(String identify, String triggerGroup) {
        try {
            TriggerKey triggerKey = new TriggerKey(identify, triggerGroup);
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
            TriggerKey triggerKey = new TriggerKey(jobName, jobGroup);
            if (!scheduler.checkExists(jobKey) || !scheduler.checkExists(triggerKey)) {
                return "failed";
            } else {
                scheduler.pauseTrigger(triggerKey);
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
            scheduler.resumeTriggers(GroupMatcher.anyTriggerGroup());
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
            TriggerKey triggerKey = new TriggerKey(jobName, jobGroup);
            if (!scheduler.checkExists(jobKey) || !scheduler.checkExists(triggerKey)) {
                return "failed";
            } else {
                scheduler.resumeJob(jobKey);
                scheduler.resumeTrigger(triggerKey);
                return "success";
            }
        } catch (Exception ex) {
            _log.error(ex.getMessage());
            return "failed";
        }
    }

    //删除某个任务
    public String DeleteJob(String jobName, String jobGroup) {
        try {
            JobKey jobKey = new JobKey(jobName, jobGroup);
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

    //删除所有任务
    public String DeleteAllJob() {
        try {
            // 获取有所的组
            List<String> jobGroupNameList = scheduler.getJobGroupNames();
            for (String jobGroupName : jobGroupNameList) {
                GroupMatcher<JobKey> jobKeyGroupMatcher = GroupMatcher.jobGroupEquals(jobGroupName);
                Set<JobKey> jobKeySet = scheduler.getJobKeys(jobKeyGroupMatcher);
                for (JobKey jobKey : jobKeySet) {
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
    public boolean ModifyJob(JobConfig jobConfig) {
        try {
            BaseTriggerConfig baseTriggerConfig = getTriggerConfig(jobConfig);
            TriggerKey triggerKey = TriggerKey.triggerKey(baseTriggerConfig.getIdentity(), baseTriggerConfig.getTriggerGroup());
            JobKey jobKey = new JobKey(jobConfig.getJobName(), jobConfig.getJobGroup());
            if (scheduler.checkExists(jobKey) && scheduler.checkExists(triggerKey)) {
                Trigger trigger = baseTriggerConfig.getTrigger();
                JobDetail detail = scheduler.getJobDetail(jobKey);
                detail.getJobDataMap().put("params", jobConfig.getInvokeParam());
                trigger.getJobDataMap().put("params", jobConfig.getInvokeParam());
                scheduler.rescheduleJob(triggerKey, trigger);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            _log.error(e.getMessage());
            return false;
        }
    }

    private BaseTriggerConfig getTriggerConfig(JobConfig jobConfig) {
        BaseTriggerConfig baseTriggerConfig;
        switch (jobConfig.getTriggerType()) {
            default:
            case 0:
                baseTriggerConfig = JSONUtils.json2pojo(jobConfig.getTriggerConfig(), SimpleBaseTriggerConfig.class);
                break;
            case 1:
                baseTriggerConfig = JSONUtils.json2pojo(jobConfig.getTriggerConfig(), CronTriggerConfig.class);
                break;
            case 2:
                baseTriggerConfig = JSONUtils.json2pojo(jobConfig.getTriggerConfig(), DailyTimeIntervalBaseTriggerConfig.class);
                break;
            case 3:
                baseTriggerConfig = JSONUtils.json2pojo(jobConfig.getTriggerConfig(), CalendarIntervalBaseTriggerConfig.class);
                break;
        }
        return baseTriggerConfig;
    }
}
