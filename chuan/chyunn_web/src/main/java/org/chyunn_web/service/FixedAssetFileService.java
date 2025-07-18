package org.chyunn_web.service;

import org.chyunn_web.bean.Asset.FixedAssetFile;
import org.chyunn_web.bean.Asset.InventoryEquipmentFile;
import org.chyunn_web.bean.incident.IncidentFile;
import org.chyunn_web.dto.FixedAssetFileDto;
import org.chyunn_web.dto.InventoryEquipmentFileDto;
import org.chyunn_web.repository.FixedAssetFileRepository;
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
public class FixedAssetFileService {
    @Autowired
    FixedAssetFileRepository fixedAssetFileRepository;
    private ModelMapper modelMapper = new ModelMapper();

    public FixedAssetFileDto converFixedAssetFile(FixedAssetFile fixedAssetFile) {
        // 自動映射
        return modelMapper.map(fixedAssetFile, FixedAssetFileDto.class);
    }

    public FixedAssetFile getFixedAssetFile(String imageId) {
        Optional<FixedAssetFile> optional = fixedAssetFileRepository.findById(imageId);
        if (optional.isPresent()) {
            FixedAssetFile fixedAssetFile = optional.get();
            return fixedAssetFile;
        } else {
            return null;
        }
    }
    public void deleteFile(String id) throws IOException {
        FixedAssetFile fixedAssetFile=getFixedAssetFile(id);
        // 路徑前綴（注意雙斜線與 escape）
        String basePath = "\\\\192.168.2.3\\群運共用夾\\※資訊部專用\\沈世泉\\inventory_file\\fixedAsset_file_";
        String fullPath = basePath + fixedAssetFile.getFixedAsset().getSub_category_id() + "\\" + fixedAssetFile.getFileName();
        Path filePath = Paths.get(fullPath);
        Files.deleteIfExists(filePath); // 確保檔案存在時才刪除
        fixedAssetFileRepository.delete(fixedAssetFile);
    }
}
