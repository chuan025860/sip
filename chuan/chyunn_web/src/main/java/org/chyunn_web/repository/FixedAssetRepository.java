package org.chyunn_web.repository;

import org.chyunn_web.bean.Asset.FixedAsset;
import org.chyunn_web.bean.Asset.Inventory_Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FixedAssetRepository extends JpaRepository<FixedAsset,String> {

    @Query("SELECT i FROM FixedAsset i " +
            "WHERE LOWER(i.sub_category_id) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(i.custodian_id) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(i.fixed_location) LIKE LOWER(CONCAT('%', :keyword, '%'))"+
            "OR LOWER(i.sub_category_name) LIKE LOWER(CONCAT('%', :keyword, '%'))"+
            "OR LOWER(i.custodian_name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<FixedAsset> findByKeyword(@Param("keyword") String keyword);

    @Query("SELECT i FROM FixedAsset i where i.fixed_location=:location")
    List<FixedAsset> filterLocation(String location);
}
