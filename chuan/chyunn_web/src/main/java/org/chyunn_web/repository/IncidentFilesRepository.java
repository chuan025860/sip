package org.chyunn_web.repository;

import org.chyunn_web.bean.incident.IncidentFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentFilesRepository extends JpaRepository<IncidentFile,String> {
}
