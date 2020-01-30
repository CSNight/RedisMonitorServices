package csnight.redis.monitor.busi.task;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.db.jpa.RmsInstance;
import csnight.redis.monitor.db.jpa.RmsJobInfo;
import csnight.redis.monitor.db.repos.RmsInsRepository;
import csnight.redis.monitor.db.repos.RmsJobRepository;
import csnight.redis.monitor.quartz.JobFactory;
import csnight.redis.monitor.rest.task.dto.TaskConfDto;
import csnight.redis.monitor.utils.BaseUtils;
import csnight.redis.monitor.utils.IdentifyUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class TaskManagerImpl {
    @Resource
    private RmsJobRepository jobRepository;
    @Resource
    private RmsInsRepository insRepository;
    @Resource
    private JobFactory jobFactory;

    public List<RmsJobInfo> GetAllJob() {
        List<RmsJobInfo> jobs = jobRepository.findAll();
        for (RmsJobInfo job : jobs) {
        }
        return jobs;
    }

    public RmsJobInfo addRedisJob(TaskConfDto taskConfDto) {
        RmsJobInfo job = new RmsJobInfo();
        RmsInstance instance = insRepository.findOnly(taskConfDto.getIns_id());
        if (instance == null) {
            return null;
        }
        job.setInstance(instance);
        job.setCreate_time(new Date());
        job.setCreate_user(BaseUtils.GetUserFromContext());
        job.setJob_group(taskConfDto.getJobGroup());
        job.setJob_name(IdentifyUtils.string2MD5(instance.getId(), taskConfDto.getJobGroup() + "$"));
        job.setJob_config(JSONObject.toJSONString(taskConfDto));
        job.setJob_describe(taskConfDto.getDescription());
        job.getJob_class();
        job.setTrigger_type(taskConfDto.getTriggerType());
        return job;
    }
}
