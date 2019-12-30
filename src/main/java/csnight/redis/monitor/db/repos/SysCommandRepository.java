package csnight.redis.monitor.db.repos;

import csnight.redis.monitor.db.jpa.SysCommands;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SysCommandRepository extends JpaRepository<SysCommands, String> {
    @Query(value = "select * from sys_commands where role_id=?", nativeQuery = true)
    SysCommands findByRoleId(String role_id);
}
