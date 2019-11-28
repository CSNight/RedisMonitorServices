package csnight.redis.monitor.db.repos;

import csnight.redis.monitor.db.jpa.RmsInstance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RmsInsRepository extends JpaRepository<RmsInstance, String> {
}
