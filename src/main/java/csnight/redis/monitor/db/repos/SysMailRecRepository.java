package csnight.redis.monitor.db.repos;

import csnight.redis.monitor.db.jpa.SysMailRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysMailRecRepository extends JpaRepository<SysMailRecord, String> {
}
