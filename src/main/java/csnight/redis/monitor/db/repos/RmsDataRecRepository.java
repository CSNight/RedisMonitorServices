package csnight.redis.monitor.db.repos;

import csnight.redis.monitor.db.jpa.RmsDataRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author csnight
 * @description
 * @since 2020/1/20 14:41
 */
public interface RmsDataRecRepository extends JpaRepository<RmsDataRecord, String> {
    RmsDataRecord findByFilename(String filename);

    @Query(value = "select * from rms_data_record where create_user=?", nativeQuery = true)
    List<RmsDataRecord> findByCreateUser(String usr);
}
