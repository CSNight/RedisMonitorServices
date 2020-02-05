package csnight.redis.monitor.db.repos;

import csnight.redis.monitor.db.jpa.RmsRcsLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RmsRcsRepository extends JpaRepository<RmsRcsLog, String> {
}
