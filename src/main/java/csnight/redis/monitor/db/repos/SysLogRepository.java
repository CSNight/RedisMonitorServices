package csnight.redis.monitor.db.repos;

import csnight.redis.monitor.db.jpa.SysOpLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface SysLogRepository extends JpaRepository<SysOpLog, String>, JpaSpecificationExecutor<SysOpLog> {
    Page<SysOpLog> findByUn(String username, Pageable pageable);

    @Transactional
    @Modifying
    @Query(value = "delete from rmsdb.sys_operation_log where un=?", nativeQuery = true)
    void deleteByUn(String user);
}
