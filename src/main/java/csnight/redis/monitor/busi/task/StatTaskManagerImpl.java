package csnight.redis.monitor.busi.task;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.db.jpa.RmsInstance;
import csnight.redis.monitor.db.jpa.RmsJobInfo;
import csnight.redis.monitor.db.repos.RmsInsRepository;
import csnight.redis.monitor.db.repos.RmsJobRepository;
import csnight.redis.monitor.db.repos.RmsMonRuleRepository;
import csnight.redis.monitor.exception.ValidateException;
import csnight.redis.monitor.monitor.MonitorBus;
import csnight.redis.monitor.quartz.JobFactory;
import csnight.redis.monitor.quartz.config.JobConfig;
import csnight.redis.monitor.quartz.config.JobGroup;
import csnight.redis.monitor.quartz.jobs.Job_CommandExec;
import csnight.redis.monitor.quartz.jobs.Job_ReportError;
import csnight.redis.monitor.quartz.jobs.Job_StatisticCollect;
import csnight.redis.monitor.redis.pool.MultiRedisPool;
import csnight.redis.monitor.redis.pool.RedisPoolInstance;
import csnight.redis.monitor.rest.task.dto.StatTaskConfDto;
import csnight.redis.monitor.utils.BaseUtils;
import csnight.redis.monitor.utils.IdentifyUtils;
import org.quartz.Job;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class StatTaskManagerImpl {
    @Resource
    private RmsJobRepository jobRepository;
    @Resource
    private RmsInsRepository insRepository;
    @Resource
    private RmsMonRuleRepository ruleRepository;
    @Resource
    private JobFactory jobFactory;

    public List<JSONObject> GetAllJob() {
        List<RmsJobInfo> jobs = jobRepository.findByJobGroupAndUser(JobGroup.STATISTIC.name(), "%");
        return GetJobState(jobs);
    }

    public List<JSONObject> GetUserJob() {
        List<RmsJobInfo> jobs = jobRepository.findByJobGroupAndUser(JobGroup.STATISTIC.name(), "%" + BaseUtils.GetUserFromContext() + "%");
        return GetJobState(jobs);
    }

    private List<JSONObject> GetJobState(List<RmsJobInfo> jobs) {
        List<JSONObject> res = new ArrayList<>();
        for (RmsJobInfo job : jobs) {
            JSONObject joJob = JSONObject.parseObject(JSONObject.toJSONString(job));
            boolean exists = jobFactory.ExistsJob(job.getJob_name(), job.getJob_group());
            joJob.put("exists", exists);
            if (exists) {
                String state = jobFactory.GetJobState(job.getJob_name(), job.getJob_group());
                if (state.equals("failed")) {
                    joJob.put("state", "NotFound");
                }
                joJob.put("state", state);
            }
            res.add(joJob);
        }
        jobs.clear();
        return res;
    }

    public JSONObject GetJobById(String jobId) {
        Optional<RmsJobInfo> optJobInfo = jobRepository.findById(jobId);
        if (optJobInfo.isPresent()) {
            RmsJobInfo jobInfo = optJobInfo.get();
            JSONObject joJob = JSONObject.parseObject(JSONObject.toJSONString(jobInfo));
            boolean exists = jobFactory.ExistsJob(jobInfo.getJob_name(), jobInfo.getJob_group());
            joJob.put("exists", exists);
            if (exists) {
                String state = jobFactory.GetJobState(jobInfo.getJob_name(), jobInfo.getJob_group());
                if (state.equals("failed")) {
                    joJob.put("state", "NotFound");
                }
                joJob.put("state", state);
            }
            return joJob;
        }
        return null;
    }

    public RmsJobInfo addRedisStatJob(StatTaskConfDto dto) {
        RmsJobInfo job = new RmsJobInfo();
        String jobName = IdentifyUtils.string2MD5(dto.getIns_id(), "Stat_");
        if (!checkStatJobConflict(jobName, JobGroup.STATISTIC.name())) {
            throw new ValidateException("Redis statistic job already exists!");
        }
        RmsInstance instance = insRepository.findOnly(dto.getIns_id());
        if (instance == null) {
            throw new ValidateException("Redis instance not found!");
        }
        job.setInstance(instance);
        job.setCreate_time(new Date());
        job.setCreate_user(BaseUtils.GetUserFromContext());
        job.setJob_group(JobGroup.STATISTIC.name());
        job.setTrigger_type(dto.getTriggerType());
        job.setJob_describe(dto.getDescription());
        JobConfig jobConfig = InitializeMonitorJob(dto, instance.getId());
        job.setJob_name(jobConfig.getJobName());
        job.setJob_config(JSONObject.toJSONString(jobConfig));
        Class<? extends Job> jobClazz = getJobByGroup(job.getJob_group());
        job.setJob_class(jobClazz.getName());
        if (jobFactory.AddJob(jobConfig, jobClazz).equals("success")) {
            //注册监控规则到监控总线，并启用已经存在的监控规则
            MonitorBus.getIns().registerJobRules(job.getJob_name());
            return jobRepository.save(job);
        }
        return null;
    }

    public RmsJobInfo ModifyRedisStatJobConf(StatTaskConfDto dto) {
        RmsInstance instance = insRepository.findOnly(dto.getIns_id());
        if (instance == null) {
            throw new ValidateException("Redis instance not found!");
        }
        String jobName = IdentifyUtils.string2MD5(instance.getId(), "Stat_");
        String jobGroup = JobGroup.STATISTIC.name();
        RmsJobInfo jobInfo = jobRepository.findByJobGroupAndJobName(jobGroup, jobName);
        boolean exists = jobFactory.ExistsJob(jobName, jobGroup);
        if (jobInfo != null && exists) {
            JobConfig jobConfig = InitializeMonitorJob(dto, instance.getId());
            String state = jobFactory.GetJobState(jobName, jobGroup);
            boolean updateRes = jobFactory.ModifyJob(jobConfig);
            if (updateRes) {
                if (state.equals("NORMAL")) {
                    jobFactory.ResumeJob(jobName, jobGroup);
                }
                jobInfo.setJob_config(JSONObject.toJSONString(jobConfig));
                jobInfo.setTrigger_type(jobConfig.getTriggerType());
                jobInfo.setJob_describe(jobConfig.getDescription());
                return jobRepository.save(jobInfo);
            }
            throw new ValidateException("Redis statistic job update failed!");
        }
        return null;
    }

    public String ModifyRedisStatJobState(String ins, boolean state) {
        if (state) {
            return RecoverRedisStatJob(ins);
        } else {
            return StopRedisStatJob(ins);
        }
    }

    public String ModifyRedisStatJobData(String ins, String cid, String appId) {
        String jobName = IdentifyUtils.string2MD5(ins, "Stat_");
        String jobGroup = JobGroup.STATISTIC.name();
        RmsJobInfo jobInfo = jobRepository.findByJobGroupAndJobName(jobGroup, jobName);
        boolean exists = jobFactory.ExistsJob(jobName, jobGroup);
        if (jobInfo != null && exists) {
            Map<String, String> params = (Map<String, String>) jobFactory.GetJobData(jobName, jobGroup);
            params.put("cid", cid);
            params.put("appId", appId);
            return jobFactory.SetJobData(jobName, jobGroup, params) ? "success" : "failed";
        }
        return "failed";
    }

    public String RecoverRedisStatJob(String ins_id) {
        RmsInstance instance = insRepository.findOnly(ins_id);
        if (instance == null) {
            return "Redis instance not found!";
        }
        RedisPoolInstance pool = MultiRedisPool.getInstance().getPool(instance.getId());
        if (!instance.isState() || pool == null) {
            return "Redis does not connect! please go to instance config page to connect first";
        }
        String jobName = IdentifyUtils.string2MD5(ins_id, "Stat_");
        String jobGroup = JobGroup.STATISTIC.name();
        RmsJobInfo jobInfo = jobRepository.findByJobGroupAndJobName(jobGroup, jobName);
        boolean exists = jobFactory.ExistsJob(jobName, jobGroup);
        if (jobInfo != null && exists) {
            return jobFactory.ResumeJob(jobName, jobGroup);
        }
        return "Job dose not found";
    }

    public String StopRedisStatJob(String ins_id) {
        String jobName = IdentifyUtils.string2MD5(ins_id, "Stat_");
        String jobGroup = JobGroup.STATISTIC.name();
        RmsJobInfo jobInfo = jobRepository.findByJobGroupAndJobName(jobGroup, jobName);
        boolean exists = jobFactory.ExistsJob(jobName, jobGroup);
        if (jobInfo != null && exists) {
            MonitorBus.getIns().destroyMonitorForJob(jobInfo.getJob_name());
            return jobFactory.PauseJob(jobName, jobGroup);
        }
        return "failed";
    }

    public String DeleteRedisStatJob(String ins_id) {
        String jobName = IdentifyUtils.string2MD5(ins_id, "Stat_");
        String jobGroup = JobGroup.STATISTIC.name();
        RmsJobInfo jobInfo = jobRepository.findByJobGroupAndJobName(jobGroup, jobName);
        //销毁所有监控器并禁用监控规则
        if (jobInfo != null) {
            MonitorBus.getIns().unregisterJobRules(jobInfo.getJob_name());
        }
        boolean exists = jobFactory.ExistsJob(jobName, jobGroup);
        if (jobInfo != null && exists) {
            String pauseRes = jobFactory.PauseJob(jobName, jobGroup);
            String delRes = jobFactory.DeleteJob(jobName, jobGroup);
            if (pauseRes.equals("success") && delRes.equals("success")) {
                jobRepository.delete(jobInfo);
                return "success";
            }
            return "Job instance stop and delete failure";
        }
        return "Job not found";
    }

    private JobConfig InitializeMonitorJob(StatTaskConfDto dto, String instance) {
        JobConfig jobConfig = JSONObject.parseObject(JSONObject.toJSONString(dto), JobConfig.class);
        jobConfig.setJobGroup(JobGroup.STATISTIC.name());
        jobConfig.setJobName(IdentifyUtils.string2MD5(instance, "Stat_"));
        JSONObject triggerConf = JSONObject.parseObject(jobConfig.getTriggerConfig());
        triggerConf.put("identity", jobConfig.getJobName());
        jobConfig.setTriggerConfig(JSONObject.toJSONString(triggerConf));
        Map<String, String> statistic = new HashMap<>();
        statistic.put("ins_id", instance);
        statistic.put("appId", "");
        statistic.put("cid", "");
        statistic.put("uid", dto.getUid());
        jobConfig.setInvokeParam(statistic);
        return jobConfig;
    }

    private boolean checkStatJobConflict(String jobName, String jobGroup) {
        RmsJobInfo jobInfo = jobRepository.findByJobGroupAndJobName(jobGroup, jobName);
        return jobInfo == null;
    }

    private Class<? extends Job> getJobByGroup(String jobGroup) {
        switch (JobGroup.getEnumType(jobGroup)) {
            default:
            case STATISTIC:
                return Job_StatisticCollect.class;
            case EXECUTION:
                return Job_CommandExec.class;
            case ERROR:
                return Job_ReportError.class;
        }
    }
}
