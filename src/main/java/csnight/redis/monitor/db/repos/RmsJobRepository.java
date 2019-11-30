package csnight.redis.monitor.db.repos;

import csnight.redis.monitor.db.jpa.RmsJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RmsJobRepository extends JpaRepository<RmsJob, String> {
}
