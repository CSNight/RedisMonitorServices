package com.csnight.redis.monitor.rest.sys;

import com.alibaba.fastjson.JSONObject;
import com.csnight.redis.monitor.aop.LogBack;
import com.csnight.redis.monitor.quartz.JobFactory;
import com.csnight.redis.monitor.quartz.config.JobConfig;
import com.csnight.redis.monitor.quartz.jobs.JobInstance;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@Api(tags = "任务管理API")
public class JobManager {
    private final JobFactory jobFactory;

    public JobManager(JobFactory jobFactory) {
        this.jobFactory = jobFactory;
    }

    @LogBack
    @RequestMapping(value = "/addJob", method = RequestMethod.POST)
    public String addjob(@Valid @RequestBody JobConfig jobConfig) throws Exception {
        return jobFactory.AddJob(jobConfig, JobInstance.class);
    }

    @LogBack
    @RequestMapping(value = "/delJob", method = RequestMethod.DELETE)
    public void deljob() throws Exception {
        JobConfig jobConfigBase = new JobConfig();
        jobConfigBase.setJobName("5bb3c849-ad30-4b94-b8d8-137fba6798e5");
        jobConfigBase.setJobGroup("ssss");
        jobConfigBase.setInvokeParam("");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("identity", jobConfigBase.getJobName());
        jsonObject.put("description", "");
        jsonObject.put("triggerGroup", "ssss");
        jsonObject.put("strategy", "");
        jsonObject.put("expression", "0/3 * * * * ?");
        jobConfigBase.setTriggerConfig(jsonObject.toJSONString());
        jobConfigBase.setTriggerType(1);
        jobFactory.ModifyJob(jobConfigBase);
    }
}
