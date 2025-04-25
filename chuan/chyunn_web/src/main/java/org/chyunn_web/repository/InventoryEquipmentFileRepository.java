package org.chyunn_web.repository;

import org.chyunn_web.bean.IncidentFile;
import org.chyunn_web.bean.InventoryEquipmentFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryEquipmentFileRepository  extends JpaRepository<InventoryEquipmentFile,String> {
}
