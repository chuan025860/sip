package org.chyunn_web.repository;

import org.chyunn_web.bean.incident.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Integer> {

    @Query("""
      SELECT r
        FROM Report r
       WHERE r.incident.incidentId = :incidentId
       ORDER BY r.reportTime DESC
      """)
    List<Report> findByIncidentIdOrderedDesc(@Param("incidentId") Integer incidentId);

}
