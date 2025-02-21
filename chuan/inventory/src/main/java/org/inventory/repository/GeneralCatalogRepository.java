package org.inventory.repository;

import org.inventory.bean.General_Catalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GeneralCatalogRepository extends JpaRepository<General_Catalog, String> {
    @Query("FROM General_Catalog i WHERE i.storage_location = :location")
    List<General_Catalog> findByLocation(@Param("location") String location);
    @Query("FROM General_Catalog i WHERE i.storage_location = :location and i.state=:state")
    List<General_Catalog> findByLocationAndState(@Param("location") String location, @Param("state") Boolean state);
}

