package csnight.redis.monitor.db.repos;

import csnight.redis.monitor.db.jpa.SysMailRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SysMailRecRepository extends JpaRepository<SysMailRecord, String>, JpaSpecificationExecutor<SysMailRecord> {
    @Query(value = "select * from sys_mail_record where create_user=? order by ct desc", nativeQuery = true)
    List<SysMailRecord> findAllByUser(String user);
}
