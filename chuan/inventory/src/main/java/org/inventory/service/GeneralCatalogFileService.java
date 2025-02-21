package org.inventory.service;

import org.inventory.bean.GeneralCatalogFile;
import org.inventory.bean.InventoryEquipmentFile;
import org.inventory.dto.General_CatalogFile_dto;
import org.inventory.dto.InventoryEquipmentFile_dto;
import org.inventory.repository.GeneralCatalogFileRepository;
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
public class GeneralCatalogFileService {
    @Autowired
    GeneralCatalogFileRepository generalCatalogFileRepository;

    private ModelMapper modelMapper = new ModelMapper();

    public General_CatalogFile_dto convertGeneral_CatalogFile_ToGeneral_CatalogFile_dto(GeneralCatalogFile generalCatalogFile) {
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
        GeneralCatalogFile generalCatalogFile=getGeneralCatalogFile(id);
        Path filePath = Paths.get(generalCatalogFile.getFilePath());
        Files.deleteIfExists(filePath); // 確保檔案存在時才刪除
        generalCatalogFileRepository.delete(generalCatalogFile);
    }
}
