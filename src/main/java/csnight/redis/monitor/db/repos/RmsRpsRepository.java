package csnight.redis.monitor.db.repos;

import csnight.redis.monitor.db.jpa.RmsRpsLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RmsRpsRepository extends JpaRepository<RmsRpsLog, String> {
}
