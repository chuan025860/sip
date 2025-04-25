package org.chyunn_web.repository;

import org.chyunn_web.bean.Location;
import org.chyunn_web.bean.ReportFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location,Integer> {
}
