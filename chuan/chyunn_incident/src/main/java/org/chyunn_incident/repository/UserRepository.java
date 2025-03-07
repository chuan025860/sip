package org.chyunn_incident.repository;

import org.chyunn_incident.bean.Incident;
import org.chyunn_incident.bean.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository  extends JpaRepository<User, String> {

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.userId = :userId")
    Boolean existsByUserId(@Param("userId") String userId);
}
