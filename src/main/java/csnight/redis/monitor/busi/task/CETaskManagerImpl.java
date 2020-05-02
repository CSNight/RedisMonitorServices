package csnight.redis.monitor.busi.task;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.db.jpa.RmsInstance;
import csnight.redis.monitor.db.jpa.RmsJobInfo;
import csnight.redis.monitor.db.repos.RmsInsRepository;
import csnight.redis.monitor.db.repos.RmsJobRepository;
import csnight.redis.monitor.exception.ValidateException;
import csnight.redis.monitor.msg.CmdExecMsgDispatcher;
import csnight.redis.monitor.msg.handler.ExecMsgHandler;
import csnight.redis.monitor.msg.series.RedisCmdType;
import csnight.redis.monitor.quartz.JobFactory;
import csnight.redis.monitor.quartz.config.JobConfig;
import csnight.redis.monitor.quartz.config.JobGroup;
import csnight.redis.monitor.quartz.jobs.Job_CommandExec;
import csnight.redis.monitor.quartz.jobs.Job_StatisticCollect;
import csnight.redis.monitor.redis.pool.MultiRedisPool;
import csnight.redis.monitor.redis.pool.RedisPoolInstance;
import csnight.redis.monitor.rest.task.dto.ExecTaskConfDto;
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


    public RmsJobInfo addCmdExeJob(ExecTaskConfDto dto) {
        RmsJobInfo job = new RmsJobInfo();
        RmsInstance instance = insRepository.findOnly(dto.getIns_id());
        if (instance == null) {
            throw new ValidateException("Redis instance not found!");
        }
        String jobName = "EXE_" + IdentifyUtils.getUUID2();
        job.setJob_name(jobName);
        job.setInstance(instance);
        job.setCreate_time(new Date());
        job.setCreate_user(BaseUtils.GetUserFromContext());
        job.setJob_group(JobGroup.EXECUTION.name());
        job.setTrigger_type(dto.getTriggerType());
        job.setJob_describe(dto.getDescription());
        dto.setJobName(jobName);
        JobConfig jobConfig = InitializeExecuteJob(dto, instance.getId());
        job.setJob_config(JSONObject.toJSONString(jobConfig));
        Class<? extends Job> jobClazz = getJobByGroup(job.getJob_group());
        job.setJob_class(jobClazz.getName());
        if (jobFactory.AddJob(jobConfig, jobClazz).equals("success")) {
            return jobRepository.save(job);
        }
        return null;
    }

    public RmsJobInfo ModifyRedisCeJobConf(ExecTaskConfDto dto) {
        RmsInstance instance = insRepository.findOnly(dto.getIns_id());
        if (instance == null) {
            throw new ValidateException("Redis instance not found!");
        }
        String jobName = dto.getJobName();
        String jobGroup = JobGroup.EXECUTION.name();
        RmsJobInfo jobInfo = jobRepository.findByJobGroupAndJobName(jobGroup, jobName);
        boolean exists = jobFactory.ExistsJob(jobName, jobGroup);
        if (jobInfo != null && exists) {
            JobConfig jobConfig = InitializeExecuteJob(dto, instance.getId());
            String state = jobFactory.GetJobState(jobName, jobGroup);
            boolean updateRes = jobFactory.ModifyJob(jobConfig);
            if (updateRes) {
                if (state.equals("NORMAL")) {
                    jobFactory.ResumeJob(jobName, jobGroup);
                }
                jobInfo.setInstance(instance);
                jobInfo.setJob_config(JSONObject.toJSONString(jobConfig));
                jobInfo.setTrigger_type(jobConfig.getTriggerType());
                jobInfo.setJob_describe(jobConfig.getDescription());
                return jobRepository.save(jobInfo);
            }
            throw new ValidateException("Redis command exec job update failed!");
        } else if (jobInfo != null && checkNeedRebuild(jobInfo)) {
            JobConfig config = InitializeExecuteJob(dto, instance.getId());
            ExecMsgHandler handler = CmdExecMsgDispatcher.getIns().getHandlerByJobId(jobInfo.getId());
            if (handler != null) {
                Map<String, String> params = (Map<String, String>) config.getInvokeParam();
                params.put("appId", handler.getAppId());
                params.put("cid", handler.getChannelId());
            }
            Class<? extends Job> jobClazz = getJobByGroup(jobInfo.getJob_group());
            if (jobFactory.AddJob(config, jobClazz).equals("success")) {
                jobInfo.setJob_config(JSONObject.toJSONString(config));
                jobInfo.setTrigger_type(dto.getTriggerType());
                jobInfo.setJob_describe(dto.getDescription());
                jobInfo.setInstance(instance);
                return jobRepository.save(jobInfo);
            }
        }
        return null;
    }

    public String ModifyRedisCeJobState(String jobId, boolean state) {
        if (state) {
            return RecoverRedisCeJob(jobId);
        } else {
            return StopRedisCeJob(jobId);
        }
    }

    public String ModifyRedisCeJobData(String jobId, String cid, String appId) {
        Optional<RmsJobInfo> optJobInfo = jobRepository.findById(jobId);
        if (optJobInfo.isEmpty()) {
            return "Job data not found!";
        }
        RmsJobInfo jobInfo = optJobInfo.get();
        boolean exists = jobFactory.ExistsJob(jobInfo.getJob_name(), jobInfo.getJob_group());
        if (exists) {
            Map<String, String> params = (Map<String, String>) jobFactory.GetJobData(jobInfo.getJob_name(), jobInfo.getJob_group());
            params.put("cid", cid);
            params.put("appId", appId);
            return jobFactory.SetJobData(jobInfo.getJob_name(), jobInfo.getJob_group(), params) ? "success" : "failed";
        }
        return "failed";
    }

    public String RecoverRedisCeJob(String jobId) {
        Optional<RmsJobInfo> optJobInfo = jobRepository.findById(jobId);
        if (optJobInfo.isEmpty()) {
            return "Job data not found!";
        }
        RmsJobInfo jobInfo = optJobInfo.get();
        RmsInstance instance = insRepository.findOnly(jobInfo.getInstance().getId());
        if (instance == null) {
            return "Redis instance not found!";
        }
        RedisPoolInstance pool = MultiRedisPool.getInstance().getPool(instance.getId());
        if (!instance.isState() || pool == null) {
            return "Redis does not connect! please go to instance config page to connect first";
        }
        boolean exists = jobFactory.ExistsJob(jobInfo.getJob_name(), jobInfo.getJob_group());
        if (exists) {
            if (jobFactory.GetJobState(jobInfo.getJob_name(), jobInfo.getJob_group()).equals("NORMAL")) {
                return "success";
            }
            return jobFactory.ResumeJob(jobInfo.getJob_name(), jobInfo.getJob_group());
        } else if (checkNeedRebuild(jobInfo)) {
            return "Job has been destroy for completed! if you want redo it please click edit button to reconfig";
        }
        return "Job dose not found";
    }

    public String StopRedisCeJob(String jobId) {
        Optional<RmsJobInfo> optJobInfo = jobRepository.findById(jobId);
        if (optJobInfo.isEmpty()) {
            return "Job data not found!";
        }
        RmsJobInfo jobInfo = optJobInfo.get();
        boolean exists = jobFactory.ExistsJob(jobInfo.getJob_name(), jobInfo.getJob_group());
        if (exists) {
            if (!jobFactory.GetJobState(jobInfo.getJob_name(), jobInfo.getJob_group()).equals("NORMAL")) {
                return "success";
            }
            return jobFactory.PauseJob(jobInfo.getJob_name(), jobInfo.getJob_group());
        } else if (checkNeedRebuild(jobInfo)) {
            return "Job has been destroy for completed! if you want redo it please click edit button to reconfig";
        }
        return "failed";
    }

    public String DeleteRedisCeJob(String jobId) {
        Optional<RmsJobInfo> optJobInfo = jobRepository.findById(jobId);
        if (optJobInfo.isEmpty()) {
            return "Job data not found!";
        }
        RmsJobInfo jobInfo = optJobInfo.get();
        boolean exists = jobFactory.ExistsJob(jobInfo.getJob_name(), jobInfo.getJob_group());
        if (exists) {
            String pauseRes = jobFactory.PauseJob(jobInfo.getJob_name(), jobInfo.getJob_group());
            String delRes = jobFactory.DeleteJob(jobInfo.getJob_name(), jobInfo.getJob_group());
            if (pauseRes.equals("success") && delRes.equals("success")) {
                jobRepository.delete(jobInfo);
                return "success";
            }
            return "Job instance stop and delete failure";
        } else if (checkNeedRebuild(jobInfo)) {
            jobRepository.delete(jobInfo);
            return "success";
        }
        return "Job not found";
    }

    private boolean checkNeedRebuild(RmsJobInfo jobInfo) {
        JobConfig config = JSONObject.parseObject(jobInfo.getJob_config(), JobConfig.class);
        JSONObject triggerConf = JSONObject.parseObject(config.getTriggerConfig());
        if (jobInfo.getTrigger_type() == 0 && triggerConf.getIntValue("repeatCount") != -1) {
            return true;
        }
        return false;
    }

    private JobConfig InitializeExecuteJob(ExecTaskConfDto dto, String instance) {
        JobConfig jobConfig = JSONObject.parseObject(JSONObject.toJSONString(dto), JobConfig.class);
        jobConfig.setJobGroup(JobGroup.EXECUTION.name());
        jobConfig.setJobName(dto.getJobName());
        JSONObject triggerConf = JSONObject.parseObject(jobConfig.getTriggerConfig());
        triggerConf.put("identity", dto.getJobName());
        jobConfig.setTriggerConfig(JSONObject.toJSONString(triggerConf));
        Map<String, String> execution = new HashMap<>();
        execution.put("ins_id", instance);
        execution.put("appId", "");
        execution.put("cid", "");
        execution.put("db", String.valueOf(dto.getDb()));
        execution.put("times", "0");
        execution.put("uid", dto.getUid());
        if (!checkCmd(dto.getInvokeParam().toString())) {
            throw new ValidateException("Command check error");
        }
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
        }
    }

    /**
     * 功能描述: 命令检查
     *
     * @param cmd 命令
     * @return : boolean
     * @author csnight
     * @since 2019/12/27 8:52
     */
    private boolean checkCmd(String cmd) {
        if (cmd != null && !cmd.equals("")) {
            String[] parts = cmd.split(" ");
            List<String> ps = new LinkedList<>();
            for (String p : parts) {
                if (!p.equals("")) {
                    ps.add(p);
                }
            }
            parts = ps.toArray(new String[]{});
            ps.clear();
            ps = null;
            RedisCmdType command = RedisCmdType.getEnumType(parts[0].toUpperCase());
            return checkCommandDisabled(command);
        }
        return false;
    }

    private boolean checkCommandDisabled(RedisCmdType cmdType) {
        if (cmdType.equals(RedisCmdType.UNKNOWN)) {
            return false;
        } else if (cmdType.equals(RedisCmdType.SUBSCRIBE)) {
            return false;
        } else if (cmdType.equals(RedisCmdType.PSUBSCRIBE)) {
            return false;
        } else if (cmdType.equals(RedisCmdType.MONITOR)) {
            return false;
        } else if (cmdType.equals(RedisCmdType.KEYS)) {
            return false;
        } else if (cmdType.equals(RedisCmdType.DEBUG)) {
            return false;
        } else if (cmdType.equals(RedisCmdType.DUMP)) {
            return false;
        } else if (cmdType.equals(RedisCmdType.SYNC)) {
            return false;
        } else if (cmdType.equals(RedisCmdType.PSYNC)) {
            return false;
        } else if (cmdType.equals(RedisCmdType.CLIENT)) {
            return false;
        } else if (cmdType.equals(RedisCmdType.FLUSHALL)) {
            return false;
        } else if (cmdType.equals(RedisCmdType.FLUSHDB)) {
            return false;
        } else if (cmdType.equals(RedisCmdType.MULTI)) {
            return false;
        } else {
            return true;
        }
    }
}
