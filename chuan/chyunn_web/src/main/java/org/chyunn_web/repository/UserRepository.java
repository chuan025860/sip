package org.chyunn_web.repository;

import org.chyunn_web.bean.User.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository  extends JpaRepository<User, String> {

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.userId = :userId")
    Boolean existsByUserId(@Param("userId") String userId);

    @Query("SELECT u FROM User u WHERE u.loginId = :loginId")
    User findByLoginId(@Param("loginId") String loginId);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END " +
            "FROM User u WHERE u.loginId = :loginId")
    Boolean existsByLoginId(@Param("loginId") String loginId);

    @Query("SELECT u FROM User u WHERE u.department.id = :departmentId")
    List<User> findByDepartmentId(Integer departmentId);

    @Query(value = """
    SELECT * FROM user
    ORDER BY
      CASE
        WHEN loginId = 'administrator' THEN 0
        WHEN loginId = 'BA000' THEN 1
        WHEN loginId LIKE 'BA%' THEN 2
        ELSE 3
      END,
      CASE
        WHEN loginId LIKE 'BA%' THEN CAST(SUBSTRING(loginId, 3) AS UNSIGNED)
        ELSE NULL
      END
""", nativeQuery = true)
    Page<User> findAllWithSpecialOrder(Pageable pageabled);
}
