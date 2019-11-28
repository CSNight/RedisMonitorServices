package com.redis.monitor.db.repos;

import com.redis.monitor.db.jpa.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SysUserRepository extends JpaRepository<SysUser, String>, JpaSpecificationExecutor<SysUser> {
    SysUser findByUsernameOrEmail(String name, String email);

    SysUser findByEmail(String email);

    SysUser findByUsername(String name);

    SysUser findByPhone(String phone);

    List<SysUser> findAllByEnabled(boolean enable);

    SysUser findByUsernameAndPassword(String username, String password);

    @Query(value = "select * from sys_user where nick_name=?", nativeQuery = true)
    SysUser findByNickName(String nickname);

    @Query(value = "select * from sys_user where org_id=?", nativeQuery = true)
    List<SysUser> findByOrgId(Long org_id);
}
