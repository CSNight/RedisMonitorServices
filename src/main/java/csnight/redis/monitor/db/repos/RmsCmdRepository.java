package csnight.redis.monitor.db.repos;

import csnight.redis.monitor.db.jpa.RmsCmdPermits;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RmsCmdRepository extends JpaRepository<RmsCmdPermits, String> {
    @Query(value = "select * from rms_cmd_permits where user_id=?", nativeQuery = true)
    List<RmsCmdPermits> findByUserId(String user_id);

    @Query(value = "select * from rms_cmd_permits where user_id=? and ins_id=?", nativeQuery = true)
    RmsCmdPermits findByUserIdAndInsId(String user_id, String ins_id);
}
