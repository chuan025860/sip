package org.chyunn_incident.repository;

import org.chyunn_incident.bean.Report;
import org.chyunn_incident.bean.TestUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestUserRepository  extends JpaRepository<TestUser, String> {
}
