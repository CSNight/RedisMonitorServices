package csnight.redis.monitor.busi.task;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.db.jpa.RmsInstance;
import csnight.redis.monitor.db.jpa.RmsJobInfo;
import csnight.redis.monitor.db.repos.RmsInsRepository;
import csnight.redis.monitor.db.repos.RmsJobRepository;
import csnight.redis.monitor.exception.ValidateException;
import csnight.redis.monitor.quartz.JobFactory;
import csnight.redis.monitor.quartz.config.JobConfig;
import csnight.redis.monitor.quartz.config.JobGroup;
import csnight.redis.monitor.quartz.jobs.Job_CommandExec;
import csnight.redis.monitor.quartz.jobs.Job_ReportError;
import csnight.redis.monitor.quartz.jobs.Job_StatisticCollect;
import csnight.redis.monitor.redis.pool.MultiRedisPool;
import csnight.redis.monitor.redis.pool.RedisPoolInstance;
import csnight.redis.monitor.redis.statistic.StatisticCollector;
import csnight.redis.monitor.rest.task.dto.TaskConfDto;
import csnight.redis.monitor.utils.BaseUtils;
import csnight.redis.monitor.utils.IdentifyUtils;
import org.quartz.Job;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class StatTaskManagerImpl {
    @Resource
    private RmsJobRepository jobRepository;
    @Resource
    private RmsInsRepository insRepository;
    @Resource
    private JobFactory jobFactory;

    public List<JSONObject> GetAllJob() {
        List<JSONObject> res = new ArrayList<>();
        List<RmsJobInfo> jobs = jobRepository.findAll();
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
        return res;
    }

    public List<JSONObject> GetUserJob() {
        List<JSONObject> res = new ArrayList<>();
        List<RmsJobInfo> jobs = jobRepository.findAll();
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
        return res;
    }

    public List<String> getJobGroup() {
        List<String> groupName = new ArrayList<>();
        JobGroup[] groups = JobGroup.values();
        for (JobGroup group : groups) {
            if (group.name().equals("UNKNOWN")) {
                continue;
            }
            groupName.add(group.name());
        }
        return groupName;
    }

    public RmsJobInfo addRedisStatJob(TaskConfDto dto) {
        RmsJobInfo job = new RmsJobInfo();
        String jobName = IdentifyUtils.string2MD5(dto.getIns_id(), "Stat$");
        if (!checkStatJobConflict(jobName, JobGroup.STATISTIC.name())) {
            throw new ValidateException("Redis statistic job already exists!");
        }
        RmsInstance instance = insRepository.findOnly(dto.getIns_id());
        if (instance == null) {
            throw new ValidateException("Redis instance not found!");
        }
        RedisPoolInstance pool = MultiRedisPool.getInstance().getPool(instance.getId());
        if (!instance.isState() || pool == null) {
            throw new ValidateException("Redis does not connect! please go to instance config page to connect first");
        }
        job.setInstance(instance);
        job.setCreate_time(new Date());
        job.setCreate_user(BaseUtils.GetUserFromContext());
        job.setJob_group(JobGroup.STATISTIC.name());
        job.setTrigger_type(dto.getTriggerType());
        job.setJob_describe(dto.getDescription());
        JobConfig jobConfig = InitializeMonitorJob(dto, instance, pool);
        job.setJob_name(jobConfig.getJobName());
        dto.setJobName(jobConfig.getJobName());
        job.setJob_config(JSONObject.toJSONString(dto));
        Class<? extends Job> jobClazz = getJobByGroup(dto.getJobGroup());
        job.setJob_class(jobClazz.getName());
        if (jobFactory.AddJob(jobConfig, jobClazz).equals("success")) {
            return jobRepository.save(job);
        }
        return null;
    }

    public RmsJobInfo ModifyRedisStatJobConf(TaskConfDto dto) {
        RmsInstance instance = insRepository.findOnly(dto.getIns_id());
        if (instance == null) {
            throw new ValidateException("Redis instance not found!");
        }
        RedisPoolInstance pool = MultiRedisPool.getInstance().getPool(instance.getId());
        if (!instance.isState() || pool == null) {
            throw new ValidateException("Redis does not connect! please go to instance config page to connect first");
        }
        String jobName = IdentifyUtils.string2MD5(instance.getId(), "Stat$");
        String jobGroup = JobGroup.STATISTIC.name();
        RmsJobInfo jobInfo = jobRepository.findByJobGroupAndJobName(jobGroup, jobName);
        boolean exists = jobFactory.ExistsJob(jobName, jobGroup);
        if (jobInfo != null && exists) {
            JobConfig jobConfig = InitializeMonitorJob(dto, instance, pool);
            boolean updateRes = jobFactory.ModifyJob(jobConfig);
            if (updateRes) {
                jobInfo.setJob_config(JSONObject.toJSONString(dto));
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

    public String RecoverRedisStatJob(String ins_id) {
        RmsInstance instance = insRepository.findOnly(ins_id);
        if (instance == null) {
            throw new ValidateException("Redis instance not found!");
        }
        RedisPoolInstance pool = MultiRedisPool.getInstance().getPool(instance.getId());
        if (!instance.isState() || pool == null) {
            throw new ValidateException("Redis does not connect! please go to instance config page to connect first");
        }
        String jobName = IdentifyUtils.string2MD5(ins_id, "Stat$");
        String jobGroup = JobGroup.STATISTIC.name();
        RmsJobInfo jobInfo = jobRepository.findByJobGroupAndJobName(jobGroup, jobName);
        boolean exists = jobFactory.ExistsJob(jobName, jobGroup);
        if (jobInfo != null && exists) {
            jobFactory.PauseJob(jobName, jobGroup);
            StatisticCollector ps = (StatisticCollector) jobFactory.GetJobData(jobName, jobGroup);
            ps.setPool(pool);
            return jobFactory.ResumeJob(jobName, jobGroup);
        }
        return "Job dose not found";
    }

    public String StopRedisStatJob(String ins_id) {
        String jobName = IdentifyUtils.string2MD5(ins_id, "Stat$");
        String jobGroup = JobGroup.STATISTIC.name();
        RmsJobInfo jobInfo = jobRepository.findByJobGroupAndJobName(jobGroup, jobName);
        boolean exists = jobFactory.ExistsJob(jobName, jobGroup);
        if (jobInfo != null && exists) {
            return jobFactory.PauseJob(jobName, jobGroup);
        }
        return "failed";
    }

    public String DeleteRedisStatJob(String ins_id) {
        String jobName = IdentifyUtils.string2MD5(ins_id, "Stat$");
        String jobGroup = JobGroup.STATISTIC.name();
        RmsJobInfo jobInfo = jobRepository.findByJobGroupAndJobName(jobGroup, jobName);
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

    private JobConfig InitializeMonitorJob(TaskConfDto dto, RmsInstance instance, RedisPoolInstance pool) {
        JobConfig jobConfig = JSONObject.parseObject(JSONObject.toJSONString(dto), JobConfig.class);
        jobConfig.setJobGroup(JobGroup.STATISTIC.name());
        jobConfig.setJobName(IdentifyUtils.string2MD5(instance.getId(), "Stat$"));
        StatisticCollector statistic = new StatisticCollector(instance.getId());
        statistic.setPool(pool);
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
