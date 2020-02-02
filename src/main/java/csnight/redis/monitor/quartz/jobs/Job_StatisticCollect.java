package csnight.redis.monitor.quartz.jobs;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Map;

public class Job_StatisticCollect implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        Map<String, String> params = (Map<String, String>) jobDataMap.get("params");
        System.out.println(params.get("ins_id"));
        System.out.println(params.get("cid"));
        System.out.println(params.get("appId"));
    }
}
