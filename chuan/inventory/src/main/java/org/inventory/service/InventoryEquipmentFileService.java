package org.inventory.service;

import org.inventory.bean.InventoryEquipmentFile;
import org.inventory.bean.Inventory_Equipment;
import org.inventory.dto.InventoryEquipmentFile_dto;
import org.inventory.dto.Inventory_Equipment_dto;
import org.inventory.repository.InventoryEquipmentFileRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Service
public class InventoryEquipmentFileService {

    @Autowired
    InventoryEquipmentFileRepository inventoryEquipmentFileRepository;

    private ModelMapper modelMapper = new ModelMapper();

    public InventoryEquipmentFile_dto convertEquipmentFileToEquipmentFileDto(InventoryEquipmentFile inventoryEquipmentFile) {
        // 自動映射
        return modelMapper.map(inventoryEquipmentFile, InventoryEquipmentFile_dto.class);
    }

    public InventoryEquipmentFile getInventoryEquipmentFile(String id) {
        Optional<InventoryEquipmentFile> optional = inventoryEquipmentFileRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        } else {
            return null;
        }
    }

    public void deleteFile(String id) throws IOException {
        InventoryEquipmentFile inventoryEquipmentFile=getInventoryEquipmentFile(id);
        Path filePath = Paths.get(inventoryEquipmentFile.getFilePath());
        Files.deleteIfExists(filePath); // 確保檔案存在時才刪除
        inventoryEquipmentFileRepository.delete(inventoryEquipmentFile);
    }
}
