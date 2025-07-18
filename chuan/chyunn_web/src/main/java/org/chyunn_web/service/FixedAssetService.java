package org.chyunn_web.service;

import org.chyunn_web.bean.Asset.FixedAsset;
import org.chyunn_web.bean.Asset.FixedAssetFile;
import org.chyunn_web.bean.Asset.InventoryEquipmentFile;
import org.chyunn_web.bean.Asset.Inventory_Equipment;
import org.chyunn_web.dto.FixedAssetDto;
import org.chyunn_web.dto.Inventory_EquipmentDto;
import org.chyunn_web.repository.FixedAssetRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FixedAssetService {
    @Autowired
    FixedAssetRepository fixedAssetRepository;
    public Boolean insertFixedAsset(List<FixedAsset> fixedAssetList) {
        try {
            fixedAssetRepository.saveAll(fixedAssetList);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public FixedAsset getFixedAssetDetails(String id) {
        Optional<FixedAsset> optional = fixedAssetRepository.findById(id);
        if (optional.isPresent()) {
            FixedAsset fixedAsset = optional.get();
            return fixedAsset;
        } else {
            return null;
        }
    }

    public Page<FixedAsset> findAll(Pageable pageable) {
        return fixedAssetRepository.findAll(pageable);
    }

    public FixedAsset insertfixedAsseAndFiles(FixedAsset fixedAsset, List<FixedAssetFile> fixedAssetFiles) {
        try {
            for (FixedAssetFile file : fixedAssetFiles) {
                file.setFixedAsset(fixedAsset);  // 設置反向關聯
            }
            fixedAsset.setFixedAssetFiles(fixedAssetFiles);  // 設定設備與檔案的關聯
            System.out.println("Saving Inventory Equipment with Files...");
            FixedAsset savedFixedAsset = fixedAssetRepository.save(fixedAsset);
            return savedFixedAsset;  // 儲存並返回
        } catch (Exception e) {
            System.out.println("Error during saving: " + e.getMessage());
            throw new RuntimeException("Saving Inventory_Equipment failed", e); // 重新拋出異常
        }
    }
    public List<FixedAsset> getProperty(String sub_category_id) {
        // 這裡假設你有一個名為 assetRepository 的 repository 用來查詢資料
        return fixedAssetRepository.findByKeyword(sub_category_id);
    }
    public List<FixedAsset> filterLocation(String location) {
        return fixedAssetRepository.filterLocation(location);
    }

    public List<FixedAsset> getAll() {
        List<FixedAsset> fixedAssets = fixedAssetRepository.findAll();
        return fixedAssets;
    }

    private ModelMapper modelMapper = new ModelMapper();
    public FixedAssetDto convertFixedAsset(FixedAsset fixedAsset) {
        // 自動映射
        return modelMapper.map(fixedAsset, FixedAssetDto.class);
    }
}
