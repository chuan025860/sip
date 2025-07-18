package org.chyunn_web.repository;

import org.chyunn_web.bean.Asset.Category_General_Catalog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface Category_General_CatalogReopository extends JpaRepository<Category_General_Catalog,Integer> {

    List<Category_General_Catalog> findAllByOrderByIdAsc();

}
