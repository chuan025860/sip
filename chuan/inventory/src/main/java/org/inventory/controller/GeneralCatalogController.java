package org.inventory.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.inventory.bean.GeneralCatalogFile;
import org.inventory.bean.General_Catalog;
import org.inventory.bean.InventoryEquipmentFile;
import org.inventory.bean.Inventory_Equipment;
import org.inventory.dto.General_CatalogFile_dto;
import org.inventory.dto.General_Catalog_dto;
import org.inventory.dto.InventoryEquipmentFile_dto;
import org.inventory.dto.Inventory_Equipment_dto;
import org.inventory.service.GeneralCatalogFileService;
import org.inventory.service.GeneralCatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Controller
public class GeneralCatalogController {
    @Autowired
    GeneralCatalogService generalCatalogService;
    @Autowired
    GeneralCatalogFileService generalCatalogFileService;

    @GetMapping("/inventory_generalCatalog/select_number")
    public String into_select_inventory(@RequestParam(required = false) String asset_id, Model model) {
        model.addAttribute("propertyID", asset_id);
        return "inventory_generalCatalog/select_number";
    }

    @PostMapping("/inventory_generalCatalog/search_number")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> into_search(@RequestParam String asset_id) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        General_Catalog generalCatalog = generalCatalogService.getGeneralCatalogById(asset_id);
        if (generalCatalog != null) {
            General_Catalog_dto generalCatalogDto = generalCatalogService.convertGeneral_CatalogToGeneral_Catalog_dto(generalCatalog);
            List<General_CatalogFile_dto> generalCatalogFileDtos = new ArrayList<>();
            for (GeneralCatalogFile generalCatalogFile : generalCatalog.getGeneralCatalogFiles()) {
                General_CatalogFile_dto generalCatalogFileDto = generalCatalogFileService.convertGeneral_CatalogFile_ToGeneral_CatalogFile_dto(generalCatalogFile);
                generalCatalogFileDtos.add(generalCatalogFileDto);
            }
            generalCatalogDto.setGeneralCatalogFileDtos(generalCatalogFileDtos);
            response.put("code", 200);
            response.put("message", "success");
            data.put("generalCatalogDto", generalCatalogDto);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } else {
            response.put("code", 404);
            response.put("message", "無資料");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping("/inventory_generalCatalog/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> inventory_update(@RequestPart(value = "files", required = false) List<MultipartFile> files,
                                                                @RequestPart("updatedData") String updatedData) {

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        General_Catalog generalCatalog;
        try {
            // 解析 JSON 字符串為 Incident 物件
            generalCatalog = objectMapper.readValue(updatedData, General_Catalog.class);
        } catch (JsonProcessingException e) {
            response.put("code", 400);
            response.put("message", "JSON 解析錯誤");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        try {
            General_Catalog newGeneralCatalog = generalCatalogService.getGeneralCatalogById(generalCatalog.getAsset_id());
            // 查詢該設備目前的檔案數量
            List<GeneralCatalogFile> existingFiles = newGeneralCatalog.getGeneralCatalogFiles();
            int existingFileCount = existingFiles.size();  // 目前已存在的圖片數量
            if (newGeneralCatalog != null) {
                // 上傳檔案（若有）
                List<GeneralCatalogFile> generalCatalogFiles = new ArrayList<>();
                // 用來追蹤已儲存的臨時檔案
                List<File> tempFiles = new ArrayList<>();
                String uploadDirBase = "\\\\192.168.2.3\\群運共用夾\\※資訊部專用\\沈世泉\\inventory_file\\";
                String eventFolderPath = uploadDirBase + "generalcatalog_" + newGeneralCatalog.getAsset_id();
                File dir = new File(eventFolderPath);
                // 嘗試存儲檔案
                if (files != null && !files.isEmpty()) {
                    if (!dir.exists() && !dir.mkdirs()) {
                        response.put("code", 500);
                        response.put("message", "無法建立附件存放目錄");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                    }
                    // **確保總數不超過 2**
                    if (existingFileCount + files.size() > 2) {
                        response.put("code", 400);
                        response.put("message", "該設備最多只能儲存 2 張圖片，目前已有 " + existingFileCount + " 張，新增的圖片數量超過限制");
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                    }
                    UUID uuid = UUID.randomUUID();
                    try {
                        // 儲存檔案並創建檔案記錄
                        for (MultipartFile file : files) {
                            if (!file.isEmpty()) {
                                String originalFilename = file.getOriginalFilename();
                                if (originalFilename != null) {
                                    // 儲存檔案到事件資料夾

                                    String filePath = eventFolderPath + File.separator + uuid + ".jpg";
                                    File storedFile = new File(filePath);
                                    file.transferTo(storedFile);
                                    tempFiles.add(storedFile);  // 將已儲存的檔案加入列表

                                    GeneralCatalogFile generalCatalogFile = new GeneralCatalogFile();
                                    generalCatalogFile.setFileId(String.valueOf(uuid));
                                    generalCatalogFile.setFilePath(filePath);
                                    generalCatalogFiles.add(generalCatalogFile);
                                }
                            }
                        }
                    } catch (IOException e) {
                        response.put("code", 500);
                        response.put("message", "檔案上傳失敗");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                    }
                }
                newGeneralCatalog.setName(generalCatalog.getName());
                newGeneralCatalog.setStorage_location(generalCatalog.getStorage_location());
                newGeneralCatalog.setModel(generalCatalog.getModel());
                newGeneralCatalog.setCustodian(generalCatalog.getCustodian());
                newGeneralCatalog.setChange_log(generalCatalog.getChange_log());
                newGeneralCatalog.setIs_updated(true);
                // 若有新檔案則存入
                if (!generalCatalogFiles.isEmpty()) {
                    newGeneralCatalog.getGeneralCatalogFiles().addAll(generalCatalogFiles);
                }
                try {
                    General_Catalog updateSucuess = generalCatalogService.insertGeneral_CatalogAndFiles(newGeneralCatalog, newGeneralCatalog.getGeneralCatalogFiles());
                    General_Catalog_dto generalCatalogDto = generalCatalogService.convertGeneral_CatalogToGeneral_Catalog_dto(updateSucuess);
                    List<General_CatalogFile_dto> generalCatalogFileDtos = new ArrayList<>();
                    for (GeneralCatalogFile generalCatalogFile : updateSucuess.getGeneralCatalogFiles()) {
                        General_CatalogFile_dto generalCatalogFileDto = generalCatalogFileService.convertGeneral_CatalogFile_ToGeneral_CatalogFile_dto(generalCatalogFile);
                        generalCatalogFileDtos.add(generalCatalogFileDto);
                    }
                    generalCatalogDto.setGeneralCatalogFileDtos(generalCatalogFileDtos);
                    response.put("code", 200);
                    response.put("message", "success");
                    data.put("generalCatalogDto", generalCatalogDto);
                    response.put("data", data);
                    return ResponseEntity.ok(response);
                } catch (Exception e) {
                    response.put("code", 404);
                    response.put("message", e.getMessage());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
            }
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "Internal server error");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
        response.put("code", 500);
        response.put("message", "server error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @PutMapping("/inventory_generalCatalog/updateState")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> inventory_update_state(@RequestParam String asset_id) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        try {
            General_Catalog generalCatalog = generalCatalogService.getGeneralCatalogById(asset_id);
            if (generalCatalog != null) {
                if (generalCatalog.getState()) {
                    generalCatalog.setState(false);
                } else {
                    generalCatalog.setState(true);
                }
                Optional<General_Catalog> updatedGeneralCatalog = generalCatalogService.insertGeneralCatalog(generalCatalog);
                if (updatedGeneralCatalog.isPresent()) {
                    response.put("code", 200);
                    response.put("message", "success");
                    response.put("state", updatedGeneralCatalog.get().getState());
                    return ResponseEntity.ok(response);
                }
            } else {
                response.put("code", 404);
                response.put("message", "錯誤");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "Internal server error");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
        response.put("code", 500);
        response.put("message", "server error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @GetMapping("/inventory_generalCatalog/select_location")
    public String into_select_location_inventory() {
        return "inventory_generalCatalog/select_location";
    }

    @PostMapping("/inventory_generalCatalog/search_location")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> into_search_location_inventory(@RequestParam String location,
                                                                              @RequestParam String stateString) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        Boolean state = null;
        if (!stateString.isEmpty()) {
            state = Boolean.parseBoolean(stateString);
        }
        if (!location.isEmpty() && !stateString.isEmpty()) {
            try {
                List<General_Catalog> generalCatalogs = generalCatalogService.findByLocationAndState(location, state);
                List<General_Catalog_dto> generalCatalogDtos = new ArrayList<>();
                for (General_Catalog generalCatalog : generalCatalogs) {
                    General_Catalog_dto generalCatalogDto;
                    generalCatalogDto = generalCatalogService.convertGeneral_CatalogToGeneral_Catalog_dto(generalCatalog);
                    generalCatalogDtos.add(generalCatalogDto);
                }
                response.put("code", 200);
                response.put("message", "success");
                data.put("generalCatalogs", generalCatalogDtos);
                response.put("data", data);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                response.put("code", 404);
                response.put("message", "無資料");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } else if (!location.isEmpty() && stateString.isEmpty()) {
            try {
                List<General_Catalog> generalCatalogs = generalCatalogService.findByLocation(location);
                List<General_Catalog_dto> generalCatalogDtos = new ArrayList<>();
                for (General_Catalog generalCatalog : generalCatalogs) {
                    General_Catalog_dto generalCatalogDto;
                    generalCatalogDto = generalCatalogService.convertGeneral_CatalogToGeneral_Catalog_dto(generalCatalog);
                    generalCatalogDtos.add(generalCatalogDto);
                }
                response.put("code", 200);
                response.put("message", "success");
                data.put("generalCatalogs", generalCatalogDtos);
                response.put("data", data);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                response.put("code", 404);
                response.put("message", "無資料");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } else if (location.isEmpty() && !stateString.isEmpty()) {
            response.put("code", 404);
            response.put("message", "未設定此條件");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } else {
            response.put("code", 404);
            response.put("message", "無資料");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/generalcatalog/image/{fileId}")
    public ResponseEntity<Resource> getImage(@PathVariable String fileId) {
        String filePath = generalCatalogFileService.getGeneralCatalogFile(fileId).getFilePath();
        File file = new File(filePath);
        // 確認檔案是否存在
        if (!file.exists() || !file.canRead()) {
            return ResponseEntity.notFound().build();
        }
        //FileSystemResource 是 Spring 提供的 Resource 接口 的一種具體實現，它專門用來處理本地檔案系統中的資源。
        //它的主要作用是將 Java File 對象 (java.io.File) 轉換為 Spring Resource，以便後續在 HTTP 回應中回傳檔案。
        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG) // 可根據檔案類型調整
                .body(resource);
    }

    @DeleteMapping("/generalcatalog/delete_image")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> inventory_delete_file(@RequestParam String fileId, @RequestParam String final_property_id) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        try {
            //刪除檔案方法放在service
            generalCatalogFileService.deleteFile(fileId);
            General_Catalog generalCatalog = generalCatalogService.getGeneralCatalogById(final_property_id);
            List<General_CatalogFile_dto> generalCatalogFileDtos = new ArrayList<>();
            for (GeneralCatalogFile generalCatalogFile : generalCatalog.getGeneralCatalogFiles()) {
                General_CatalogFile_dto generalCatalogFileDto = generalCatalogFileService.convertGeneral_CatalogFile_ToGeneral_CatalogFile_dto(generalCatalogFile);
                generalCatalogFileDtos.add(generalCatalogFileDto);
            }
            data.put("generalCatalogFileDtos", generalCatalogFileDtos);
            response.put("data", data);
            response.put("code", 200);
            response.put("message", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "Internal server error");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

    }
}
