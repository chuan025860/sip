package org.chyunn_web.repository;

import org.chyunn_web.bean.Asset.FixedAssetLocation;
import org.chyunn_web.bean.Asset.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FixedAssetLocationRepository extends JpaRepository<FixedAssetLocation,Integer> {
}
