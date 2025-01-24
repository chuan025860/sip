package org.inventory.repository;

import org.inventory.bean.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, String> {
    @Query("FROM Inventory i WHERE i.location = :location")
    List<Inventory> findByLocation(@Param("location") String location);
    @Query("FROM Inventory i WHERE i.location = :location and i.state=:state")
    List<Inventory> findByLocationAndState(@Param("location") String location, @Param("state") Boolean state);
}
