package csnight.redis.monitor.db.repos;

import csnight.redis.monitor.db.jpa.RmsRosLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RmsRosRepository extends JpaRepository<RmsRosLog, String> {
}
