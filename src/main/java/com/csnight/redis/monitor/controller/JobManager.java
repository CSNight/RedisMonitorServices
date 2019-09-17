package com.csnight.redis.monitor.controller;

import com.alibaba.fastjson.JSONObject;
import com.csnight.redis.monitor.quartz.JobFactory;
import com.csnight.redis.monitor.quartz.config.JobConfig;
import com.csnight.redis.monitor.quartz.jobs.JobInstance;
import com.csnight.redis.monitor.utils.GUID;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JobManager {
    private final JobFactory jobFactory;

    public JobManager(JobFactory jobFactory) {
        this.jobFactory = jobFactory;
    }

    //添加一个job
    @RequestMapping(value = "/addJob", method = RequestMethod.GET)
    public String addjob() throws Exception {
        JobConfig jobConfigBase = new JobConfig();
        jobConfigBase.setJobName(GUID.getUUID());
        jobConfigBase.setJobGroup("ssss");
        jobConfigBase.setInvokeParam("");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("identity", jobConfigBase.getJobName());
        jsonObject.put("description", "");
        jsonObject.put("jobGroup", "ssss");
        jsonObject.put("strategy", "");
        jsonObject.put("expression", "0/1 * * * * ?");
        jobConfigBase.setTriggerConfig(jsonObject.toJSONString());
        jobConfigBase.setTriggerType(0);

        return jobFactory.AddJob(jobConfigBase, JobInstance.class);
    }

    @RequestMapping(value = "/delJob", method = RequestMethod.GET)
    public void deljob() throws Exception {
        JobConfig jobConfigBase = new JobConfig();
        jobConfigBase.setJobName("bc2cffb8-610c-411e-aaf6-ecf2a44daabc");
        jobConfigBase.setJobGroup("ssss");
        jobConfigBase.setInvokeParam("");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("identity", jobConfigBase.getJobName());
        jsonObject.put("description", "");
        jsonObject.put("jobGroup", "ssss");
        jsonObject.put("strategy", "");
        jsonObject.put("expression", "0/3 * * * * ?");
        jobConfigBase.setTriggerConfig(jsonObject.toJSONString());
        jobConfigBase.setTriggerType(1);
        jobFactory.ModifyJob(jobConfigBase);
    }
}
