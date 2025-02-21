package org.inventory.service;

import org.inventory.bean.GeneralCatalogFile;
import org.inventory.bean.General_Catalog;
import org.inventory.bean.InventoryEquipmentFile;
import org.inventory.bean.Inventory_Equipment;
import org.inventory.dto.General_Catalog_dto;
import org.inventory.dto.Inventory_Equipment_dto;
import org.inventory.repository.GeneralCatalogRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GeneralCatalogService {
    @Autowired
    GeneralCatalogRepository generalCatalogRepository;

    public Boolean insertGeneralCatalogList(List<General_Catalog> generalCatalogList) {
        try {
            generalCatalogRepository.saveAll(generalCatalogList);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public General_Catalog getGeneralCatalogById(String id) {
        Optional<General_Catalog> optional = generalCatalogRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }else {
            return null;
        }
    }

    public Optional<General_Catalog> insertGeneralCatalog(General_Catalog generalCatalog) {
        try {
            General_Catalog saveGeneralCatalog = generalCatalogRepository.save(generalCatalog);
            return Optional.of(saveGeneralCatalog);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public List<General_Catalog> findByLocationAndState(String location, Boolean state) {
        List<General_Catalog> generalCatalogList = generalCatalogRepository.findByLocationAndState(location,state);
        if (generalCatalogList.isEmpty()) {
            return null;
        }else {
            return generalCatalogList;
        }
    }

    public List<General_Catalog> findByLocation(String location) {
        List<General_Catalog> generalCatalogList = generalCatalogRepository.findByLocation(location);
        if (generalCatalogList.isEmpty()) {
            return null;
        }else {
            return generalCatalogList;
        }
    }

    private ModelMapper modelMapper = new ModelMapper();

    public General_Catalog_dto convertGeneral_CatalogToGeneral_Catalog_dto(General_Catalog generalCatalog) {
        // 自動映射
        return modelMapper.map(generalCatalog, General_Catalog_dto.class);
    }

    public General_Catalog insertGeneral_CatalogAndFiles(General_Catalog generalCatalog, List<GeneralCatalogFile> generalCatalogFiles) {
        try {
            for (GeneralCatalogFile file : generalCatalogFiles) {
                file.setGeneralCatalog(generalCatalog);  // 設置反向關聯
            }
            // 確保 generalCatalog 的檔案列表不會成為序列化引起的循環
            generalCatalog.setGeneralCatalogFiles(generalCatalogFiles);  // 設定設備與檔案的關聯
            General_Catalog savedGeneral_Catalog = generalCatalogRepository.save(generalCatalog);
            return savedGeneral_Catalog;  // 儲存並返回
        } catch (Exception e) {
            System.out.println("Error during saving: " + e.getMessage());
            throw new RuntimeException("Saving Inventory_Equipment failed", e); // 重新拋出異常
        }
    }
}
