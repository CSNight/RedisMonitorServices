package csnight.redis.monitor.db.repos;

import csnight.redis.monitor.db.jpa.SysCommands;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysCommandRepository extends JpaRepository<SysCommands, String> {

}
