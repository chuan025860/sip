package org.chyunn_web.repository;

import org.chyunn_web.bean.incident.ReportFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportFilesRepository  extends JpaRepository<ReportFile,String> {
}
