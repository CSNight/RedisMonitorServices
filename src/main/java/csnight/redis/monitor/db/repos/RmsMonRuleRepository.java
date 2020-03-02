package csnight.redis.monitor.db.repos;

import csnight.redis.monitor.db.jpa.RmsMonitorRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RmsMonRuleRepository extends JpaRepository<RmsMonitorRule, String> {
    @Query(value = "select * from rms_monitor_rule where create_user=?", nativeQuery = true)
    List<RmsMonitorRule> findByUser(String user);

    RmsMonitorRule findByExpressionAndIns(String expression, String ins);
}
