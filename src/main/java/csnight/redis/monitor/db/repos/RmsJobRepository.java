package csnight.redis.monitor.db.repos;

import csnight.redis.monitor.db.jpa.RmsJobInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RmsJobRepository extends JpaRepository<RmsJobInfo, String> {
    @Query(value = "select * from rms_jobs_info where job_group=? and job_name = ?", nativeQuery = true)
    RmsJobInfo findByJobGroupAndJobName(String jobGroup, String jobName);
}
