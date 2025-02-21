package org.inventory.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.inventory.bean.InventoryEquipmentFile;
import org.inventory.bean.Inventory_Equipment;
import org.inventory.dto.InventoryEquipmentFile_dto;
import org.inventory.dto.Inventory_Equipment_dto;
import org.inventory.service.InventoryEquipmentFileService;
import org.inventory.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Controller
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private InventoryEquipmentFileService inventoryEquipmentFileService;

    @GetMapping("/invertory_index")
    public String into_index_inventory() {
        return "index/invertory_index";
    }

    @GetMapping("/inventory_equipment/select_location")
    public String into_select_location_inventory() {
        return "inventory_equipment/select_location";
    }

    @PostMapping("/inventory_equipment/search_location")
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
                List<Inventory_Equipment> inventories = inventoryService.findByLocationAndState(location, state);
                List<Inventory_Equipment_dto> inventoryEquipmentDtos = new ArrayList<>();
                for (Inventory_Equipment inventory_equipment : inventories) {
                    Inventory_Equipment_dto inventoryEquipmentDto;
                    inventoryEquipmentDto = inventoryService.convertInventory_EquipmentToInventory_EquipmentDto(inventory_equipment);
                    inventoryEquipmentDtos.add(inventoryEquipmentDto);
                }
                response.put("code", 200);
                response.put("message", "success");
                data.put("inventories", inventoryEquipmentDtos);
                response.put("data", data);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                response.put("code", 404);
                response.put("message", "無資料");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } else if (!location.isEmpty() && stateString.isEmpty()) {
            try {
                List<Inventory_Equipment> inventories = inventoryService.findByLocation(location);
                List<Inventory_Equipment_dto> inventoryEquipmentDtos = new ArrayList<>();
                for (Inventory_Equipment inventory_equipment : inventories) {
                    Inventory_Equipment_dto inventoryEquipmentDto;
                    inventoryEquipmentDto = inventoryService.convertInventory_EquipmentToInventory_EquipmentDto(inventory_equipment);
                    inventoryEquipmentDtos.add(inventoryEquipmentDto);
                }
                response.put("code", 200);
                response.put("message", "success");
                data.put("inventories", inventoryEquipmentDtos);
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

    @GetMapping("/inventory_equipment/select_number")
    public String into_select_inventory(@RequestParam(required = false) String final_property_id, Model model) {
        model.addAttribute("propertyID", final_property_id);
        return "inventory_equipment/select_number";
    }

    @PostMapping("/inventory_equipment/search_number")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> into_search(@RequestParam String final_property_id) {
        System.out.println(final_property_id);
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        Inventory_Equipment inventoryequipment = inventoryService.getInventoryById(final_property_id);
        if (inventoryequipment != null) {
            Inventory_Equipment_dto inventoryEquipmentDto = inventoryService.convertInventory_EquipmentToInventory_EquipmentDto(inventoryequipment);
            List<InventoryEquipmentFile_dto> inventoryEquipmentFileDtos = new ArrayList<>();
            for (InventoryEquipmentFile inventoryEquipmentFile : inventoryequipment.getInventoryEquipmentFiles()) {
                InventoryEquipmentFile_dto inventoryEquipmentFileDto = inventoryEquipmentFileService.convertEquipmentFileToEquipmentFileDto(inventoryEquipmentFile);
                inventoryEquipmentFileDtos.add(inventoryEquipmentFileDto);
            }
            inventoryEquipmentDto.setInventoryEquipmentFileDtos(inventoryEquipmentFileDtos);
            response.put("code", 200);
            response.put("message", "success");
            data.put("inventoryEquipmentDto", inventoryEquipmentDto);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } else {
            response.put("code", 404);
            response.put("message", "無資料");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping("/inventory_equipment/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> inventory_update(@RequestPart(value = "files", required = false) List<MultipartFile> files,
                                                                @RequestPart("updatedData") String updatedData) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        Inventory_Equipment inventoryEquipment;
        try {
            // 解析 JSON 字符串為 Incident 物件
            inventoryEquipment = objectMapper.readValue(updatedData, Inventory_Equipment.class);
        } catch (JsonProcessingException e) {
            response.put("code", 400);
            response.put("message", "JSON 解析錯誤");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            Inventory_Equipment newInventoryEquipment = inventoryService.getInventoryById(inventoryEquipment.getFinal_property_id());
            // 查詢該設備目前的檔案數量
            List<InventoryEquipmentFile> existingFiles = newInventoryEquipment.getInventoryEquipmentFiles();
            int existingFileCount = existingFiles.size();  // 目前已存在的圖片數量

            if (newInventoryEquipment != null) {
                // 上傳檔案（若有）
                List<InventoryEquipmentFile> inventoryEquipmentFiles = new ArrayList<>();
                // 用來追蹤已儲存的臨時檔案
                List<File> tempFiles = new ArrayList<>();
//                String uploadDirBase = "C:\\Users\\gagood72\\Desktop\\chuan\\inventory_file\\";
                String uploadDirBase = "\\\\192.168.2.3\\群運共用夾\\※資訊部專用\\沈世泉\\inventory_file\\";
                String eventFolderPath = uploadDirBase + "equipment_" + newInventoryEquipment.getFinal_property_id();
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

                                    InventoryEquipmentFile inventoryEquipmentFile = new InventoryEquipmentFile();
                                    inventoryEquipmentFile.setFileId(String.valueOf(uuid));
                                    inventoryEquipmentFile.setFilePath(filePath);
                                    inventoryEquipmentFiles.add(inventoryEquipmentFile);
                                }
                            }
                        }
                    } catch (IOException e) {
                        response.put("code", 500);
                        response.put("message", "檔案上傳失敗");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                    }
                }
                newInventoryEquipment.setName(inventoryEquipment.getName());
                newInventoryEquipment.setLocation(inventoryEquipment.getLocation());
                newInventoryEquipment.setRemarks(inventoryEquipment.getRemarks());
                newInventoryEquipment.setChange_record(inventoryEquipment.getChange_record());
                newInventoryEquipment.setIs_updated(true);

                // 若有新檔案則存入
                if (!inventoryEquipmentFiles.isEmpty()) {
                    newInventoryEquipment.getInventoryEquipmentFiles().addAll(inventoryEquipmentFiles);
                }
                try {
                    Inventory_Equipment updateSucuess = inventoryService.insertInventoryAndFiles(newInventoryEquipment, newInventoryEquipment.getInventoryEquipmentFiles());
                    Inventory_Equipment_dto inventoryEquipmentDto = inventoryService.convertInventory_EquipmentToInventory_EquipmentDto(updateSucuess);
                    List<InventoryEquipmentFile_dto> inventoryEquipmentFileDtos = new ArrayList<>();
                    for (InventoryEquipmentFile inventoryEquipmentFile : updateSucuess.getInventoryEquipmentFiles()) {
                        InventoryEquipmentFile_dto inventoryEquipmentFileDto = inventoryEquipmentFileService.convertEquipmentFileToEquipmentFileDto(inventoryEquipmentFile);
                        inventoryEquipmentFileDtos.add(inventoryEquipmentFileDto);
                    }
                    inventoryEquipmentDto.setInventoryEquipmentFileDtos(inventoryEquipmentFileDtos);
                    response.put("code", 200);
                    response.put("message", "success");
                    data.put("inventoryEquipmentDto", inventoryEquipmentDto);
                    response.put("data", data);
                    return ResponseEntity.ok(response);
                } catch (Exception e) {
                    response.put("code", 404);
                    response.put("message", e.getMessage());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
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
    }

    @PutMapping("/inventory_equipment/updateState")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> inventory_update_state(@RequestParam String final_property_id) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        try {
            Inventory_Equipment inventoryequipment = inventoryService.getInventoryById(final_property_id);
            if (inventoryequipment != null) {
                if (inventoryequipment.getState()) {
                    inventoryequipment.setState(false);
                } else {
                    inventoryequipment.setState(true);
                }
                Optional<Inventory_Equipment> updatedInventory = inventoryService.insertInventory(inventoryequipment);
                if (updatedInventory.isPresent()) {
                    response.put("code", 200);
                    response.put("message", "success");
                    response.put("state", updatedInventory.get().getState());
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


    @GetMapping("/inventory_equipment/image/{fileId}")
    public ResponseEntity<Resource> getImage(@PathVariable String fileId) {
        String filePath = inventoryEquipmentFileService.getInventoryEquipmentFile(fileId).getFilePath();
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

    @DeleteMapping("/inventory_equipment/delete_image")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> inventory_delete_file(@RequestParam String fileId, @RequestParam String final_property_id) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        try {
            //刪除檔案方法放在service
            inventoryEquipmentFileService.deleteFile(fileId);
            Inventory_Equipment inventoryequipment = inventoryService.getInventoryById(final_property_id);
            List<InventoryEquipmentFile_dto> inventoryEquipmentFileDtos = new ArrayList<>();
            for (InventoryEquipmentFile inventoryEquipmentFile : inventoryequipment.getInventoryEquipmentFiles()) {
                InventoryEquipmentFile_dto inventoryEquipmentFileDto = inventoryEquipmentFileService.convertEquipmentFileToEquipmentFileDto(inventoryEquipmentFile);
                inventoryEquipmentFileDtos.add(inventoryEquipmentFileDto);
            }
            data.put("inventoryEquipmentFileDtos", inventoryEquipmentFileDtos);
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
