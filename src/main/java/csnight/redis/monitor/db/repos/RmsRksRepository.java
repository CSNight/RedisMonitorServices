package csnight.redis.monitor.db.repos;

import csnight.redis.monitor.db.jpa.RmsRksLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RmsRksRepository extends JpaRepository<RmsRksLog, String> {
}