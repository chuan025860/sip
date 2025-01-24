package org.chyunn_incident.repository;

import org.chyunn_incident.bean.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident,String> {
    @Query("SELECT i FROM Incident i WHERE i.invalid = false ORDER BY i.reportDate DESC")
    List<Incident> findAllByReportDateDescAndInvalidFalse();

    @Query("SELECT i FROM Incident i WHERE i.invalid = true ORDER BY i.reportDate DESC")
    List<Incident> findAllByInvalid();

    @Query("SELECT i FROM Incident i WHERE i.status = :status ORDER BY i.reportDate DESC")
    List<Incident> findAllByStatus(String status);
}
