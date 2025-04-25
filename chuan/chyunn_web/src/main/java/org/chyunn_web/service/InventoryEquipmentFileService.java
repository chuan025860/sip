package org.chyunn_web.service;

import org.chyunn_web.bean.Incident;
import org.chyunn_web.bean.InventoryEquipmentFile;
import org.chyunn_web.dto.IncidentDto;
import org.chyunn_web.dto.InventoryEquipmentFileDto;
import org.chyunn_web.repository.InventoryEquipmentFileRepository;
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

    public InventoryEquipmentFileDto convertInventoryEquipmentFileDto(InventoryEquipmentFile inventoryEquipmentFile) {
        // 自動映射
        return modelMapper.map(inventoryEquipmentFile, InventoryEquipmentFileDto.class);
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
        // 路徑前綴（注意雙斜線與 escape）
        String basePath = "\\\\192.168.2.3\\群運共用夾\\※資訊部專用\\沈世泉\\inventory_file\\equipment_";
        String fullPath = basePath + inventoryEquipmentFile.getInventoryEquipment().getFinal_property_id() + "\\" + inventoryEquipmentFile.getFileName();
        Path filePath = Paths.get(fullPath);
        Files.deleteIfExists(filePath); // 確保檔案存在時才刪除
        inventoryEquipmentFileRepository.delete(inventoryEquipmentFile);
    }
}
