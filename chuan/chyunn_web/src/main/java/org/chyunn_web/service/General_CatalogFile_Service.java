package org.chyunn_web.service;

import org.chyunn_web.bean.Asset.GeneralCatalogFile;
import org.chyunn_web.dto.General_CatalogFile_dto;
import org.chyunn_web.repository.GeneralCatalogFileRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Service
public class General_CatalogFile_Service {
    @Autowired
    GeneralCatalogFileRepository generalCatalogFileRepository;

    private ModelMapper modelMapper = new ModelMapper();

    public General_CatalogFile_dto convertInventoryEquipmentFileDto(GeneralCatalogFile generalCatalogFile) {
        // 自動映射
        return modelMapper.map(generalCatalogFile, General_CatalogFile_dto.class);
    }


    public GeneralCatalogFile getGeneralCatalogFile(String id) {
        Optional<GeneralCatalogFile> optional = generalCatalogFileRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        } else {
            return null;
        }
    }

    public void deleteFile(String id) throws IOException {
        GeneralCatalogFile generalCatalogFile = getGeneralCatalogFile(id);
        // 路徑前綴（注意雙斜線與 escape）
        String basePath = "\\\\192.168.2.3\\群運共用夾\\※資訊部專用\\沈世泉\\inventory_file\\equipment_All_";
        String fullPath = basePath + generalCatalogFile.getGeneralCatalog().getAsset_id() + "\\" + generalCatalogFile.getFileName();
        Path filePath = Paths.get(fullPath);
        Files.deleteIfExists(filePath); // 確保檔案存在時才刪除
        generalCatalogFileRepository.delete(generalCatalogFile);
    }

}