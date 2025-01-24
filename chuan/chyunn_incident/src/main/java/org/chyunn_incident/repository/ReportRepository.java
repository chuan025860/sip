package org.chyunn_incident.repository;

import org.chyunn_incident.bean.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Integer> {
}
