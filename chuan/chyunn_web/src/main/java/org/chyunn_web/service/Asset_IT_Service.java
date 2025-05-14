package org.chyunn_web.service;

import jakarta.transaction.Transactional;
import org.chyunn_web.bean.InventoryEquipmentFile;
import org.chyunn_web.bean.Inventory_Equipment;
import org.chyunn_web.dto.Inventory_EquipmentDto;
import org.chyunn_web.repository.Asset_IT_Repository;
import org.chyunn_web.repository.RandomSampling_IT_Repository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class Asset_IT_Service {
    @Autowired
    Asset_IT_Repository assetITRepository;
    @Autowired
    RandomSampling_IT_Repository randomSamplingItRepository;

    public Page<Inventory_Equipment> findAll(Pageable pageable) {
        return assetITRepository.findAll(pageable);
    }

    private ModelMapper modelMapper = new ModelMapper();

    public Inventory_EquipmentDto convertInventory_EquipmentDto(Inventory_Equipment inventoryEquipment) {
        // 自動映射
        return modelMapper.map(inventoryEquipment, Inventory_EquipmentDto.class);
    }

    public Inventory_Equipment getPropertyDetails(String id) {
        Optional<Inventory_Equipment> optional = assetITRepository.findById(id);
        if (optional.isPresent()) {
            Inventory_Equipment inventoryEquipment = optional.get();
            return inventoryEquipment;
        } else {
            return null;
        }
    }

    public List<Inventory_Equipment> getProperty(String final_property_id) {
        // 這裡假設你有一個名為 assetRepository 的 repository 用來查詢資料
        return assetITRepository.findByKeyword(final_property_id);
    }

    public Inventory_Equipment updateProperty(Inventory_Equipment inventoryEquipment) {
        return assetITRepository.save(inventoryEquipment);
    }

    public Inventory_Equipment saveProperty(Inventory_Equipment inventoryEquipment) {
        // 假設 final_property_id 是主鍵或唯一欄位
        if (assetITRepository.existsById(inventoryEquipment.getFinal_property_id())) {
            throw new DataIntegrityViolationException("主鍵重複，該財產編號已存在！");
        }
        return assetITRepository.save(inventoryEquipment);
    }

    public List<Inventory_Equipment> filterLocation(String location) {
        return assetITRepository.filterLocation(location);
    }

    public List<Inventory_Equipment> getAll() {
        List<Inventory_Equipment> inventoryEquipments = assetITRepository.findAll();
        return inventoryEquipments;
    }

    public Boolean insertInventoryList(List<Inventory_Equipment> inventoryequipmentList) {
        try {
            assetITRepository.saveAll(inventoryequipmentList);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //    public List<Inventory_Equipment> findByInventoryInProgressTrue() {
//        List<Inventory_Equipment> inventoryEquipments = assetRepository.findInProgressItems();
//        return inventoryEquipments;
//    }
    public List<Inventory_Equipment> findByInventoryCheckedFalseAndInventoryInProgressFalse() {
        List<Inventory_Equipment> inventoryEquipments = assetITRepository.findByInventoryCheckedFalseAndInventoryInProgressFalse();
        return inventoryEquipments;
    }


    public void saveInventoryData(List<Inventory_Equipment> selected) {
        assetITRepository.saveAll(selected);
    }

    public Inventory_Equipment insertInventoryAndFiles(Inventory_Equipment inventoryequipment, List<InventoryEquipmentFile> inventoryEquipmentFiles) {
        try {
            for (InventoryEquipmentFile file : inventoryEquipmentFiles) {
                file.setInventoryEquipment(inventoryequipment);  // 設置反向關聯
            }
            inventoryequipment.setInventoryEquipmentFiles(inventoryEquipmentFiles);  // 設定設備與檔案的關聯
            System.out.println("Saving Inventory Equipment with Files...");
            Inventory_Equipment savedInventoryEquipment = assetITRepository.save(inventoryequipment);
            return savedInventoryEquipment;  // 儲存並返回
        } catch (Exception e) {
            System.out.println("Error during saving: " + e.getMessage());
            throw new RuntimeException("Saving Inventory_Equipment failed", e); // 重新拋出異常
        }
    }

    public List<Inventory_Equipment> findByRandomSampling_IT_ServiceIT(List<String> ids) {
        List<Inventory_Equipment> inventoryEquipments = assetITRepository.findByRandomSampling_IT_ServiceIT(ids);
        return inventoryEquipments;
    }

    @Transactional
    public void resetInventory() {
        assetITRepository.resetAllInventoryEquipment();
        randomSamplingItRepository.deleteAllSamplingData();
    }

}
