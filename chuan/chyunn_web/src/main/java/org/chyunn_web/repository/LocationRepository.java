package org.chyunn_web.repository;

import org.chyunn_web.bean.Asset.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location,Integer> {
}
