package org.chyunn_web.repository;

import org.chyunn_web.bean.incident.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Integer> {
}
