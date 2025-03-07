package org.chyunn_incident.repository;

import org.chyunn_incident.bean.IncidentFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentFilesRepository extends JpaRepository<IncidentFile,String> {
}
