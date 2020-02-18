package csnight.redis.monitor.db.repos;

import csnight.redis.monitor.db.jpa.SysMailConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysMailConfRepository extends JpaRepository<SysMailConfig, String> {
    SysMailConfig findByUid(String uid);
}
