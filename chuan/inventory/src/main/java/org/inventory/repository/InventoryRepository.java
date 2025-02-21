package org.inventory.repository;

import org.inventory.bean.Inventory_Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory_Equipment, String> {
    @Query("FROM Inventory_Equipment i WHERE i.location = :location")
    List<Inventory_Equipment> findByLocation(@Param("location") String location);
    @Query("FROM Inventory_Equipment i WHERE i.location = :location and i.state=:state")
    List<Inventory_Equipment> findByLocationAndState(@Param("location") String location, @Param("state") Boolean state);
}
