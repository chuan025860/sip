package org.inventory.repository;

import org.inventory.bean.GeneralCatalogFile;
import org.inventory.bean.General_Catalog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeneralCatalogFileRepository extends JpaRepository<GeneralCatalogFile, String> {
}
