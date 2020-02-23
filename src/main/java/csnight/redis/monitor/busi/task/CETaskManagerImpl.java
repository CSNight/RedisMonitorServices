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
import csnight.redis.monitor.rest.task.dto.TaskConfDto;
import csnight.redis.monitor.utils.BaseUtils;
import csnight.redis.monitor.utils.IdentifyUtils;
import org.quartz.Job;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class CETaskManagerImpl {
    @Resource
    private RmsJobRepository jobRepository;
    @Resource
    private RmsInsRepository insRepository;
    @Resource
    private JobFactory jobFactory;

    public List<JSONObject> GetAllJob() {
        List<RmsJobInfo> jobs = jobRepository.findByJobGroupAndUser(JobGroup.EXECUTION.name(), "%");
        List<JSONObject> res = new ArrayList<>();
        for (RmsJobInfo job : jobs) {
            GetJobState(job);
            JSONObject joJob = GetJobState(job);
            res.add(joJob);
        }
        jobs.clear();
        return res;
    }


    public List<JSONObject> GetUserJob() {
        List<RmsJobInfo> jobs = jobRepository.findByJobGroupAndUser(JobGroup.EXECUTION.name(), "%" + BaseUtils.GetUserFromContext() + "%");
        List<JSONObject> res = new ArrayList<>();
        for (RmsJobInfo job : jobs) {
            GetJobState(job);
            JSONObject joJob = GetJobState(job);
            res.add(joJob);
        }
        jobs.clear();
        return res;
    }

    public JSONObject GetJobById(String jobId) {
        Optional<RmsJobInfo> optJobInfo = jobRepository.findById(jobId);
        if (optJobInfo.isPresent()) {
            RmsJobInfo jobInfo = optJobInfo.get();
            return GetJobState(jobInfo);
        }
        return null;
    }


    public RmsJobInfo addCmdExeJob(TaskConfDto dto) {
        RmsJobInfo job = new RmsJobInfo();
        RmsInstance instance = insRepository.findOnly(dto.getIns_id());
        if (instance == null) {
            throw new ValidateException("Redis instance not found!");
        }
        job.setInstance(instance);
        job.setCreate_time(new Date());
        job.setCreate_user(BaseUtils.GetUserFromContext());
        job.setJob_group(JobGroup.EXECUTION.name());
        job.setTrigger_type(dto.getTriggerType());
        job.setJob_describe(dto.getDescription());
        JobConfig jobConfig = InitializeExecuteJob(dto, instance.getId());
        job.setJob_name(jobConfig.getJobName());
        job.setJob_config(JSONObject.toJSONString(jobConfig));
        Class<? extends Job> jobClazz = getJobByGroup(job.getJob_group());
        job.setJob_class(jobClazz.getName());
        if (jobFactory.AddJob(jobConfig, jobClazz).equals("success")) {
            return jobRepository.save(job);
        }
        return null;
    }

    private JobConfig InitializeExecuteJob(TaskConfDto dto, String instance) {
        String jobName = "EXE_" + IdentifyUtils.getUUID2();
        JobConfig jobConfig = JSONObject.parseObject(JSONObject.toJSONString(dto), JobConfig.class);
        jobConfig.setJobGroup(JobGroup.EXECUTION.name());
        jobConfig.setJobName(jobName);
        JSONObject triggerConf = JSONObject.parseObject(jobConfig.getTriggerConfig());
        triggerConf.put("identity", jobName);
        jobConfig.setTriggerConfig(JSONObject.toJSONString(triggerConf));
        Map<String, String> execution = new HashMap<>();
        execution.put("ins_id", instance);
        execution.put("appId", "");
        execution.put("cid", "");
        execution.put("uid", dto.getUid());
        execution.put("exe", dto.getInvokeParam().toString());
        jobConfig.setInvokeParam(execution);
        return jobConfig;
    }

    private JSONObject GetJobState(RmsJobInfo job) {
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
        return joJob;
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
