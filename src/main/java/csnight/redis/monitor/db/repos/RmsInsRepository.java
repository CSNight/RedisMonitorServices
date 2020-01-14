package csnight.redis.monitor.db.repos;

import csnight.redis.monitor.db.jpa.RmsInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RmsInsRepository extends JpaRepository<RmsInstance, String>, JpaSpecificationExecutor<RmsInstance> {
    @Query(value = "select * from rms_instance where user_id=? order by ct", nativeQuery = true)
    List<RmsInstance> findByUserId(String user_id);

    RmsInstance findByUin(String uin);

    @Query(value = "select * from rms_instance where id=?", nativeQuery = true)
    RmsInstance findOnly(String id);

    @Query(value = "select * from rms_instance where instance_name=?", nativeQuery = true)
    RmsInstance findByInstanceName(String ins_name);

    @Query(value = "select * from rms_instance where user_id=? and mode<>'sentinel' order by ct", nativeQuery = true)
    List<RmsInstance> findAllByUserIdAndMode(String user_id);

    @Query(value = "select * from rms_instance where mode<>'sentinel' order by ct", nativeQuery = true)
    List<RmsInstance> findAllFilterByMode();
}
