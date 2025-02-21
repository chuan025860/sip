package org.inventory.service;

import org.inventory.bean.InventoryEquipmentFile;
import org.inventory.bean.Inventory_Equipment;
import org.inventory.dto.Inventory_Equipment_dto;
import org.inventory.repository.InventoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {
    @Autowired
    InventoryRepository inventoryRepository;

    public Boolean insertInventoryList(List<Inventory_Equipment> inventoryequipmentList) {
        try {
            inventoryRepository.saveAll(inventoryequipmentList);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Optional<Inventory_Equipment> insertInventory(Inventory_Equipment inventoryequipment) {
        try {
            Inventory_Equipment savedInventoryequipment = inventoryRepository.save(inventoryequipment);
            return Optional.of(savedInventoryequipment);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Inventory_Equipment insertInventoryAndFiles(Inventory_Equipment inventoryequipment, List<InventoryEquipmentFile> inventoryEquipmentFiles) {
        try {
            for (InventoryEquipmentFile file : inventoryEquipmentFiles) {
                file.setInventoryEquipment(inventoryequipment);  // 設置反向關聯
            }

            // 確保 inventoryequipment 的檔案列表不會成為序列化引起的循環
            inventoryequipment.setInventoryEquipmentFiles(inventoryEquipmentFiles);  // 設定設備與檔案的關聯

            System.out.println("Saving Inventory Equipment with Files...");
            Inventory_Equipment savedInventoryEquipment = inventoryRepository.save(inventoryequipment);

            return savedInventoryEquipment;  // 儲存並返回
        } catch (Exception e) {
            System.out.println("Error during saving: " + e.getMessage());
            throw new RuntimeException("Saving Inventory_Equipment failed", e); // 重新拋出異常
        }
    }

    public Inventory_Equipment getInventoryById(String id) {
        Optional<Inventory_Equipment> optional = inventoryRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        } else {
            return null;
        }
    }

    public List<Inventory_Equipment> findByLocation(String location) {
        List<Inventory_Equipment> inventoryequipmentList = inventoryRepository.findByLocation(location);
        if (inventoryequipmentList.isEmpty()) {
            return null;
        } else {
            return inventoryequipmentList;
        }
    }

    public List<Inventory_Equipment> findByLocationAndState(String location, Boolean state) {
        List<Inventory_Equipment> inventoryequipmentList = inventoryRepository.findByLocationAndState(location, state);
        if (inventoryequipmentList.isEmpty()) {
            return null;
        } else {
            return inventoryequipmentList;
        }
    }

    public List<Inventory_Equipment> findAll() {
        List<Inventory_Equipment> inventoryequipmentList = inventoryRepository.findAll();
        if (inventoryequipmentList.isEmpty()) {
            return null;
        } else {
            return inventoryequipmentList;
        }
    }

    private ModelMapper modelMapper = new ModelMapper();

    public Inventory_Equipment_dto convertInventory_EquipmentToInventory_EquipmentDto(Inventory_Equipment inventoryEquipment) {
        // 自動映射
        return modelMapper.map(inventoryEquipment, Inventory_Equipment_dto.class);
    }
}
