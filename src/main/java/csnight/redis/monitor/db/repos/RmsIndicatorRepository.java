package csnight.redis.monitor.db.repos;

import csnight.redis.monitor.db.jpa.RmsIndicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RmsIndicatorRepository extends JpaRepository<RmsIndicator, String> {
    @Query(value = "select * from rms_indicators where id=?", nativeQuery = true)
    RmsIndicator findOnly(String id);

    RmsIndicator findByName(String name);

    RmsIndicator findByLabel(String label);
}
