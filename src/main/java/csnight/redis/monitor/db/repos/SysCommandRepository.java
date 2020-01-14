package csnight.redis.monitor.db.repos;

import csnight.redis.monitor.db.jpa.SysCommands;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SysCommandRepository extends JpaRepository<SysCommands, String> {
}
