package org.chyunn_web.repository;

import org.chyunn_web.bean.GeneralCatalogFile;
import org.chyunn_web.bean.InventoryEquipmentFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeneralCatalogFileRepository  extends JpaRepository<GeneralCatalogFile,String> {
}
