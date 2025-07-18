package org.chyunn_web.service;

import org.chyunn_web.bean.Asset.Category_General_Catalog;
import org.chyunn_web.repository.Category_General_CatalogReopository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Category_General_CatalogService {
    @Autowired
    Category_General_CatalogReopository categoryGeneralCatalogReopository;

    public List<Category_General_Catalog> findAllByOrderByIdAsc() {
        return categoryGeneralCatalogReopository.findAllByOrderByIdAsc();
    }

}
