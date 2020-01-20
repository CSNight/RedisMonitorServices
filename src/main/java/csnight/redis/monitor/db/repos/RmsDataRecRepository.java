package csnight.redis.monitor.db.repos;

import csnight.redis.monitor.db.jpa.RmsDataRecord;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author csnight
 * @description
 * @since 2020/1/20 14:41
 */
public interface RmsDataRecRepository extends JpaRepository<RmsDataRecord, String> {
    RmsDataRecord findByFilename(String filename);
}
