package org.chyunn_web.service;

import jakarta.transaction.Transactional;
import org.chyunn_web.bean.GeneralCatalogFile;
import org.chyunn_web.bean.General_Catalog;
import org.chyunn_web.bean.InventoryEquipmentFile;
import org.chyunn_web.bean.Inventory_Equipment;
import org.chyunn_web.dto.General_CatalogDto;
import org.chyunn_web.dto.Inventory_EquipmentDto;
import org.chyunn_web.repository.Asset_All_Repository;
import org.chyunn_web.repository.RandomSampling_All_Repository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class Asset_All_Service {
    @Autowired
    Asset_All_Repository assetAllRepository;
    @Autowired
    RandomSampling_All_Repository randomSamplingAllRepository;


    public Page<General_Catalog> findAll(Pageable pageable) {
        return assetAllRepository.findAll(pageable);
    }
    private ModelMapper modelMapper = new ModelMapper();

    public General_CatalogDto convertInventory_EquipmentDto(General_Catalog generalCatalog) {
        // 自動映射
        return modelMapper.map(generalCatalog, General_CatalogDto.class);
    }

    public List<General_Catalog> getProperty(String final_property_id) {
        // 這裡假設你有一個名為 assetRepository 的 repository 用來查詢資料
        return assetAllRepository.findByKeyword(final_property_id);
    }
    public List<General_Catalog> filterLocation(String location) {
        return assetAllRepository.filterLocation(location);
    }

    public List<General_Catalog> getAll() {
        List<General_Catalog> generalCatalogs = assetAllRepository.findAll();
        return generalCatalogs;
    }

    public General_Catalog saveProperty(General_Catalog generalCatalog) {
        // 假設 final_property_id 是主鍵或唯一欄位
        if (assetAllRepository.existsById(generalCatalog.getAsset_id())) {
            throw new DataIntegrityViolationException("主鍵重複，該財產編號已存在！");
        }
        return assetAllRepository.save(generalCatalog);
    }
    public General_Catalog updateProperty(General_Catalog generalCatalog) {
        return assetAllRepository.save(generalCatalog);
    }

    public General_Catalog getPropertyDetails(String id) {
        Optional<General_Catalog> optional = assetAllRepository.findById(id);
        if (optional.isPresent()) {
            General_Catalog generalCatalog = optional.get();
            return generalCatalog;
        } else {
            return null;
        }
    }

    public Boolean insertGeneralCatalogList(List<General_Catalog> generalCatalogList) {
        try {
            assetAllRepository.saveAll(generalCatalogList);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<General_Catalog> findByInventoryCheckedFalseAndInventoryInProgressFalse() {
        List<General_Catalog> generalCatalogs = assetAllRepository.findByInventoryCheckedFalseAndInventoryInProgressFalse();
        return generalCatalogs;
    }

    public void saveInventoryData(List<General_Catalog> selected) {
        assetAllRepository.saveAll(selected);
    }

    public List<General_Catalog> findByRandomSampling_All_ServiceAll(List<String> ids) {
        List<General_Catalog> generalCatalogs= assetAllRepository.findByRandomSampling_All_ServiceAll(ids);
        return generalCatalogs;
    }

    public General_Catalog insertInventoryAndFiles(General_Catalog generalCatalog, List<GeneralCatalogFile> generalCatalogFiles) {
        try {
            for (GeneralCatalogFile file : generalCatalogFiles) {
                file.setGeneralCatalog(generalCatalog);  // 設置反向關聯
            }
            generalCatalog.setGeneralCatalogFiles(generalCatalogFiles);  // 設定設備與檔案的關聯
            System.out.println("Saving Inventory Equipment with Files...");
            General_Catalog savedGeneral_Catalog= assetAllRepository.save(generalCatalog);
            return savedGeneral_Catalog;  // 儲存並返回
        } catch (Exception e) {
            System.out.println("Error during saving: " + e.getMessage());
            throw new RuntimeException("Saving Inventory_Equipment failed", e); // 重新拋出異常
        }
    }

    @Transactional
    public void resetInventory() {
        assetAllRepository.resetAllInventoryEquipment();
        randomSamplingAllRepository.deleteAllSamplingData();
    }
}
