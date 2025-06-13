package org.chyunn_web.repository;

import org.chyunn_web.bean.User.UserRole;
import org.chyunn_web.bean.User.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.user.loginId = :loginId")
    void deleteByLoginId(@Param("loginId") String loginId);

    @Query("SELECT ur.id.role FROM UserRole ur WHERE ur.id.id = :loginId")
    List<String> findRolesByLoginId(String loginId);
}
