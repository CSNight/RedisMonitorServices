package csnight.redis.monitor.db.repos;

import csnight.redis.monitor.db.jpa.RmsJobInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RmsJobRepository extends JpaRepository<RmsJobInfo, String> {
    @Query(value = "select * from rms_jobs_info where job_group=? and job_name = ?", nativeQuery = true)
    RmsJobInfo findByJobGroupAndJobName(String jobGroup, String jobName);

    @Query(value = "select * from rms_jobs_info where job_group=? and create_user like ?", nativeQuery = true)
    List<RmsJobInfo> findByJobGroupAndUser(String jobGroup, String jobName);
}
