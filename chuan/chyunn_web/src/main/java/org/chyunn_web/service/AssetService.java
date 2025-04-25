package org.chyunn_web.service;

import org.chyunn_web.bean.Incident;
import org.chyunn_web.bean.InventoryEquipmentFile;
import org.chyunn_web.bean.Inventory_Equipment;
import org.chyunn_web.dto.IncidentDto;
import org.chyunn_web.dto.Inventory_EquipmentDto;
import org.chyunn_web.repository.AssetRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AssetService {
    @Autowired
    AssetRepository assetRepository;

    public Page<Inventory_Equipment> findAll(Pageable pageable) {
        return assetRepository.findAll(pageable);
    }

    private ModelMapper modelMapper = new ModelMapper();

    public Inventory_EquipmentDto convertInventory_EquipmentDto(Inventory_Equipment inventoryEquipment) {
        // 自動映射
        return modelMapper.map(inventoryEquipment, Inventory_EquipmentDto.class);
    }

    public Inventory_Equipment getPropertyDetails(String id) {
        assetRepository.findById(id);
        Optional<Inventory_Equipment> optional = assetRepository.findById(id);
        if (optional.isPresent()) {
            Inventory_Equipment inventoryEquipment = optional.get();
            return inventoryEquipment;
        } else {
            return null;
        }
    }

    public Page<Inventory_Equipment> getProperty(String final_property_id, Pageable pageable) {
        // 這裡假設你有一個名為 assetRepository 的 repository 用來查詢資料
        return assetRepository.findByKeyword(final_property_id, pageable);
    }

    public Inventory_Equipment updateProperty(Inventory_Equipment inventoryEquipment) {
        return assetRepository.save(inventoryEquipment);
    }
    public Inventory_Equipment saveProperty(Inventory_Equipment inventoryEquipment) {
        // 假設 final_property_id 是主鍵或唯一欄位
        if (assetRepository.existsById(inventoryEquipment.getFinal_property_id())) {
            throw new DataIntegrityViolationException("主鍵重複，該財產編號已存在！");
        }
       return assetRepository.save(inventoryEquipment);
    }

    public Page<Inventory_Equipment> filterLocation(String location, Pageable pageable) {
        return assetRepository.filterLocation(location,pageable);
    }

    public List<Inventory_Equipment> getAll() {
        List<Inventory_Equipment> inventoryEquipments = assetRepository.findAll();
        return inventoryEquipments;
    }

    public Boolean insertInventoryList(List<Inventory_Equipment> inventoryequipmentList) {
        try {
            assetRepository.saveAll(inventoryequipmentList);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public List<Inventory_Equipment> findByInventoryInProgressTrue() {
        List<Inventory_Equipment> inventoryEquipments = assetRepository.findInProgressItems();
        return inventoryEquipments;
    }
    public List<Inventory_Equipment> findByInventoryCheckedFalseAndInventoryInProgressFalse() {
        List<Inventory_Equipment> inventoryEquipments = assetRepository.findByInventoryCheckedFalseAndInventoryInProgressFalse();
        return inventoryEquipments;
    }
    public List<Inventory_Equipment> findByInventoryCheckedFalseAndInventoryInProgressTrue() {
        List<Inventory_Equipment> inventoryEquipments = assetRepository.findByInventoryCheckedFalseAndInventoryInProgressTrue();
        return inventoryEquipments;
    }
    public void saveInventoryData(List<Inventory_Equipment> selected) {
        assetRepository.saveAll(selected);
    }

    public Inventory_Equipment insertInventoryAndFiles(Inventory_Equipment inventoryequipment, List<InventoryEquipmentFile> inventoryEquipmentFiles) {
        try {
            for (InventoryEquipmentFile file : inventoryEquipmentFiles) {
                file.setInventoryEquipment(inventoryequipment);  // 設置反向關聯
            }
            inventoryequipment.setInventoryEquipmentFiles(inventoryEquipmentFiles);  // 設定設備與檔案的關聯
            System.out.println("Saving Inventory Equipment with Files...");
            Inventory_Equipment savedInventoryEquipment = assetRepository.save(inventoryequipment);
            return savedInventoryEquipment;  // 儲存並返回
        } catch (Exception e) {
            System.out.println("Error during saving: " + e.getMessage());
            throw new RuntimeException("Saving Inventory_Equipment failed", e); // 重新拋出異常
        }
    }

}
