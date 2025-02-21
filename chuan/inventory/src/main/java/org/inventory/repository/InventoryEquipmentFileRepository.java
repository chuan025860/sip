package org.inventory.repository;

import org.inventory.bean.General_Catalog;
import org.inventory.bean.InventoryEquipmentFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryEquipmentFileRepository extends JpaRepository<InventoryEquipmentFile, String> {
}
