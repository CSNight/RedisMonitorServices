package csnight.redis.monitor.db.repos;

import csnight.redis.monitor.db.jpa.RmsShakeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author csnight
 * @description
 * @since 2020-1-18 21:38
 */
public interface RmsShakeRepository extends JpaRepository<RmsShakeRecord, String> {
    @Query(value = "select * from rms_shake_recore", nativeQuery = true)
    List<RmsShakeRecord> findByCreateUser(String getUserFromContext);
}
