package org.chyunn_incident.repository;

import org.chyunn_incident.bean.IncidentFile;
import org.chyunn_incident.bean.ReportFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportFilesRepository  extends JpaRepository<ReportFile,String> {
}
