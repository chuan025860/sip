package org.chyunn_web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.chyunn_web.bean.Asset.*;
import org.chyunn_web.dto.*;
import org.chyunn_web.security.JwtTokenProvider;
import org.chyunn_web.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class MobileController {
    @Autowired
    UserService userService;
    @Autowired
    Asset_IT_Service assetITService;
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    JwtBlacklistService jwtBlacklistService;
    @Autowired
    LocationService locationService;
    @Autowired
    InventoryEquipmentFileService inventoryEquipmentFileService;
    @Autowired
    RandomSampling_IT_Service randomSamplingItService;
    @Autowired
    RandomSampling_All_Service randomSamplingAllService;
    @Autowired
    Asset_All_Service assetAllService;
    @Autowired
    General_CatalogFile_Service generalCatalogFileService;
    @Autowired
    AssetHistoryService assetHistoryService;
    @Autowired
    AssetHistory_ALL_Service assetHistoryAllService;

    @GetMapping("/mobile/asset/invertory_Index")
    public String mobile_invertory_Index() {
        return "mobile/mobile_invertory_Index";
    }

    @GetMapping("/mobile/asset/select_IT")
    public String select_IT() {
        return "mobile/mobile_select_IT";
    }

    @GetMapping("/mobile/asset/select_IT_detail")
    public String select_IT_detail(@RequestParam(required = false) String final_property_id, Model model) {
        model.addAttribute("propertyID", final_property_id);
        return "mobile/mobile_select_IT_detail";
    }

    @GetMapping("/mobile/asset/select_All")
    public String select_All() {
        return "mobile/mobile_select_All";
    }

    @GetMapping("/mobile/asset/select_All_detail")
    public String select_All_detail(@RequestParam(required = false) String asset_id, Model model) {
        model.addAttribute("propertyID", asset_id);
        return "mobile/mobile_select_All_detail";
    }

    @GetMapping("/mobile/asset/Asset_Sampling_List")
    public String into_index_inventory(Model model) {
        List<String> ids = randomSamplingItService.findIds();
        List<Inventory_Equipment> selected = assetITService.findByRandomSampling_IT_ServiceIT(ids);
//        List<Inventory_Equipment> selected = assetService.findByInventoryCheckedFalseAndInventoryInProgressTrue();
        model.addAttribute("Asset_Sampling_List", selected);
        return "mobile/Asset_Sampling_List";
    }

    @GetMapping("/mobile/asset/Asset_Sampling_All_List")
    public String into_index_inventory_All(Model model) {
        List<String> ids = randomSamplingAllService.findIds();
        List<General_Catalog> selected = assetAllService.findByRandomSampling_All_ServiceAll(ids);
//        List<Inventory_Equipment> selected = assetService.findByInventoryCheckedFalseAndInventoryInProgressTrue();
        model.addAttribute("Asset_Sampling_List", selected);
        return "mobile/Asset_Sampling_List_All";
    }


    @GetMapping("/mobile/asset/mobile_select_number")
    public String into_select_inventory(@RequestParam(required = false) String final_property_id, Model model) {
        List<String> ids = randomSamplingItService.findIds();
        List<Inventory_Equipment> selected = assetITService.findByRandomSampling_IT_ServiceIT(ids);
        String prevId = null;
        String nextId = null;
        if (final_property_id != null && !selected.isEmpty()) {
            int index = -1;
            for (int i = 0; i < selected.size(); i++) {
                if (final_property_id.equals(selected.get(i).getFinal_property_id())) {
                    index = i;
                    break;
                }
            }
            if (index > 0) {
                prevId = selected.get(index - 1).getFinal_property_id();
            }
            if (index >= 0 && index < selected.size() - 1) {
                nextId = selected.get(index + 1).getFinal_property_id();
            }
        }
        model.addAttribute("prevId", prevId);
        model.addAttribute("nextId", nextId);
        model.addAttribute("propertyID", final_property_id);
        return "mobile/mobile_select_invertory";
    }

    @GetMapping("/mobile/asset/mobile_select_number_All")
    public String into_select_inventory_All(@RequestParam(required = false) String asset_id, Model model) {
        List<String> ids = randomSamplingAllService.findIds();
        List<General_Catalog> selected = assetAllService.findByRandomSampling_All_ServiceAll(ids);
        String prevId = null;
        String nextId = null;
        if (asset_id != null && !selected.isEmpty()) {
            int index = -1;
            for (int i = 0; i < selected.size(); i++) {
                if (asset_id.equals(selected.get(i).getAsset_id())) {
                    index = i;
                    break;
                }
            }
            if (index > 0) {
                prevId = selected.get(index - 1).getAsset_id();
            }
            if (index >= 0 && index < selected.size() - 1) {
                nextId = selected.get(index + 1).getAsset_id();
            }
        }
        model.addAttribute("prevId", prevId);
        model.addAttribute("nextId", nextId);
        model.addAttribute("propertyID", asset_id);
        return "mobile/mobile_select_invertory_All";
    }

    @PostMapping("/asset/mobile_get_invertoryDetail")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> mobile_get_invertoryDetail(@RequestParam String final_property_id) {
        System.out.println(final_property_id);
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        Inventory_Equipment inventoryequipment = assetITService.getPropertyDetails(final_property_id);
        if (inventoryequipment != null) {
            Inventory_EquipmentDto inventoryEquipmentDto = assetITService.convertInventory_EquipmentDto(inventoryequipment);
            List<InventoryEquipmentFileDto> inventoryEquipmentFileDtos = new ArrayList<>();
            for (InventoryEquipmentFile inventoryEquipmentFile : inventoryequipment.getInventoryEquipmentFiles()) {
                InventoryEquipmentFileDto inventoryEquipmentFileDto = inventoryEquipmentFileService.convertInventoryEquipmentFileDto(inventoryEquipmentFile);
                inventoryEquipmentFileDto.setFinal_property_id(inventoryEquipmentFile.getInventoryEquipment().getFinal_property_id());
                inventoryEquipmentFileDtos.add(inventoryEquipmentFileDto);
            }
            List<AssetHistory> sortedHistoryList = inventoryequipment.getAssetHistoryList()
                    .stream()
                    .sorted((h1, h2) -> h2.getDate().compareTo(h1.getDate())) // 從新到舊
                    .collect(Collectors.toList());
            List<AssetHistoryDto> assetHistoryDtos = new ArrayList<>();
            for (AssetHistory assetHistory : sortedHistoryList) {
                AssetHistoryDto assetHistoryDto = assetHistoryService.convert_AssetHistoryDto(assetHistory);
                assetHistoryDtos.add(assetHistoryDto);
            }
            inventoryEquipmentDto.setAssetHistoryDtoList(assetHistoryDtos);
            inventoryEquipmentDto.setInventoryEquipmentFileDtoList(inventoryEquipmentFileDtos);
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


    @PostMapping("/asset/mobile_get_invertoryDetail_All")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> mobile_get_invertoryDetail_All(@RequestParam String asset_id) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        General_Catalog generalCatalog = assetAllService.getPropertyDetails(asset_id);
        if (generalCatalog != null) {
            General_CatalogDto generalCatalogDto = assetAllService.convertInventory_EquipmentDto(generalCatalog);
            List<General_CatalogFile_dto> generalCatalogFileDtos = new ArrayList<>();
            for (GeneralCatalogFile generalCatalogFile : generalCatalog.getGeneralCatalogFiles()) {
                General_CatalogFile_dto generalCatalogFileDto = generalCatalogFileService.convertInventoryEquipmentFileDto(generalCatalogFile);
                generalCatalogFileDto.setAsset_id(generalCatalogFile.getGeneralCatalog().getAsset_id());
                generalCatalogFileDtos.add(generalCatalogFileDto);
            }
            List<AssetHistory_All> sortedHistoryList = generalCatalog.getAssetHistoryList()
                    .stream()
                    .sorted((h1, h2) -> h2.getDate().compareTo(h1.getDate())) // 從新到舊
                    .collect(Collectors.toList());
            List<AssetHistoryDto_All> assetHistoryDtoAlls = new ArrayList<>();
            for (AssetHistory_All assetHistoryAll : sortedHistoryList) {
                AssetHistoryDto_All assetHistoryDtoAll = assetHistoryAllService.convert_AssetHistoryDto(assetHistoryAll);
                assetHistoryDtoAlls.add(assetHistoryDtoAll);
            }
            generalCatalogDto.setAssetHistoryDtoAlls(assetHistoryDtoAlls);
            generalCatalogDto.setGeneralCatalogFileDtos(generalCatalogFileDtos);
            response.put("code", 200);
            response.put("message", "success");
            data.put("inventoryEquipmentDto", generalCatalogDto);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } else {
            response.put("code", 404);
            response.put("message", "無資料");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }


    @PutMapping("/asset_manage/mobile_update_invertoryDetail")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> inventory_update(@RequestPart(value = "files", required = false) List<MultipartFile> files,
                                                                @RequestPart("updatedData") String updatedData,
                                                                @RequestPart("update_history") String updateHistoryStr) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        Inventory_Equipment inventoryEquipment;
        boolean updateHistory = Boolean.parseBoolean(updateHistoryStr); // 轉成 boolean
        try {
            // 解析 JSON 字符串為 Incident 物件
            inventoryEquipment = objectMapper.readValue(updatedData, Inventory_Equipment.class);
        } catch (JsonProcessingException e) {
            response.put("code", 400);
            response.put("message", "JSON 解析錯誤");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            Inventory_Equipment newInventoryEquipment = assetITService.getPropertyDetails(inventoryEquipment.getFinal_property_id());
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
                    // **確保總數不超過 10**
                    if (existingFileCount + files.size() > 10) {
                        response.put("code", 400);
                        response.put("message", "該設備最多只能儲存 10 張圖片，目前已有 " + existingFileCount + " 張，新增的圖片數量超過限制");
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
                                    inventoryEquipmentFile.setFileName(uuid + ".jpg");
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
                newInventoryEquipment.setCustodian(inventoryEquipment.getCustodian());
                newInventoryEquipment.setRemarks(inventoryEquipment.getRemarks());
                newInventoryEquipment.setChange_record(inventoryEquipment.getChange_record());
                if (updateHistory) {
                    AssetHistory assetHistory = new AssetHistory();
                    assetHistory.setDate(new SimpleDateFormat("yyyy/MM/dd").format(new Date()));
                    assetHistory.setCustodian(newInventoryEquipment.getCustodian());
                    assetHistory.setLocation(newInventoryEquipment.getLocation());
                    assetHistory.setChange_record(newInventoryEquipment.getChange_record());
                    assetHistory.setInventoryEquipment(newInventoryEquipment);
                    System.out.println("----------------------");
                    newInventoryEquipment.getAssetHistoryList().add(assetHistory);
                }

                // 若有新檔案則存入
                if (!inventoryEquipmentFiles.isEmpty()) {
                    newInventoryEquipment.getInventoryEquipmentFiles().addAll(inventoryEquipmentFiles);
                }
                try {
                    Inventory_Equipment updateSucuess = assetITService.insertInventoryAndFiles(newInventoryEquipment, newInventoryEquipment.getInventoryEquipmentFiles());
                    Inventory_EquipmentDto inventoryEquipmentDto = assetITService.convertInventory_EquipmentDto(updateSucuess);
                    List<InventoryEquipmentFileDto> inventoryEquipmentFileDtos = new ArrayList<>();
                    for (InventoryEquipmentFile inventoryEquipmentFile : updateSucuess.getInventoryEquipmentFiles()) {
                        InventoryEquipmentFileDto inventoryEquipmentFileDto = inventoryEquipmentFileService.convertInventoryEquipmentFileDto(inventoryEquipmentFile);
                        inventoryEquipmentFileDtos.add(inventoryEquipmentFileDto);
                    }
                    List<AssetHistory> sortedHistoryList = updateSucuess.getAssetHistoryList()
                            .stream()
                            .sorted((h1, h2) -> h2.getDate().compareTo(h1.getDate())) // 從新到舊
                            .collect(Collectors.toList());
                    List<AssetHistoryDto> assetHistoryDtos = new ArrayList<>();
                    for (AssetHistory assetHistory :sortedHistoryList) {
                        AssetHistoryDto assetHistoryDto = assetHistoryService.convert_AssetHistoryDto(assetHistory);
                        assetHistoryDtos.add(assetHistoryDto);
                    }
                    inventoryEquipmentDto.setAssetHistoryDtoList(assetHistoryDtos);
                    inventoryEquipmentDto.setInventoryEquipmentFileDtoList(inventoryEquipmentFileDtos);
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

    @PutMapping("/asset_manage/mobile_update_invertoryDetail_All")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> inventory_update_ALL(@RequestPart(value = "files", required = false) List<MultipartFile> files,
                                                                    @RequestPart("updatedData") String updatedData,
                                                                    @RequestPart("update_history") String updateHistoryStr) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        General_Catalog generalCatalog;
        boolean updateHistory = Boolean.parseBoolean(updateHistoryStr); // 轉成 boolean
        try {
            // 解析 JSON 字符串為 Incident 物件
            generalCatalog = objectMapper.readValue(updatedData, General_Catalog.class);
        } catch (JsonProcessingException e) {
            response.put("code", 400);
            response.put("message", "JSON 解析錯誤");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            General_Catalog newGeneralCatalog = assetAllService.getPropertyDetails(generalCatalog.getAsset_id());
            // 查詢該設備目前的檔案數量
            List<GeneralCatalogFile> existingFiles = newGeneralCatalog.getGeneralCatalogFiles();
            int existingFileCount = existingFiles.size();  // 目前已存在的圖片數量

            if (newGeneralCatalog != null) {
                // 上傳檔案（若有）
                List<GeneralCatalogFile> generalCatalogFiles = new ArrayList<>();
                // 用來追蹤已儲存的臨時檔案
                List<File> tempFiles = new ArrayList<>();
//                String uploadDirBase = "C:\\Users\\gagood72\\Desktop\\chuan\\inventory_file\\";
                String uploadDirBase = "\\\\192.168.2.3\\群運共用夾\\※資訊部專用\\沈世泉\\inventory_file\\";
                String eventFolderPath = uploadDirBase + "equipment_All_" + newGeneralCatalog.getAsset_id();
                File dir = new File(eventFolderPath);
                // 嘗試存儲檔案
                if (files != null && !files.isEmpty()) {
                    if (!dir.exists() && !dir.mkdirs()) {
                        response.put("code", 500);
                        response.put("message", "無法建立附件存放目錄");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                    }
                    // **確保總數不超過 2**
                    if (existingFileCount + files.size() > 10) {
                        response.put("code", 400);
                        response.put("message", "該設備最多只能儲存 10 張圖片，目前已有 " + existingFileCount + " 張，新增的圖片數量超過限制");
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
                                    generalCatalogFile.setFileName(uuid + ".jpg");
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
                newGeneralCatalog.setChange_log(generalCatalog.getChange_log());
                newGeneralCatalog.setCustodian(generalCatalog.getCustodian());
                // 若有新檔案則存入
                if (!generalCatalogFiles.isEmpty()) {
                    newGeneralCatalog.getGeneralCatalogFiles().addAll(generalCatalogFiles);
                }
                if (updateHistory) {
                    AssetHistory_All assetHistory = new AssetHistory_All();
                    assetHistory.setDate(new SimpleDateFormat("yyyy/MM/dd").format(new Date()));
                    assetHistory.setCustodian(newGeneralCatalog.getCustodian());
                    assetHistory.setLocation(newGeneralCatalog.getStorage_location());
                    assetHistory.setChange_record(newGeneralCatalog.getChange_log());
                    assetHistory.setGeneralCatalog(newGeneralCatalog);
                    System.out.println("----------------------");
                    newGeneralCatalog.getAssetHistoryList().add(assetHistory);
                }
                try {
                    General_Catalog updateSucuess = assetAllService.insertInventoryAndFiles(newGeneralCatalog, newGeneralCatalog.getGeneralCatalogFiles());
                    General_CatalogDto generalCatalogDto = assetAllService.convertInventory_EquipmentDto(updateSucuess);
                    List<General_CatalogFile_dto> generalCatalogFileDtos = new ArrayList<>();
                    for (GeneralCatalogFile generalCatalogFile : updateSucuess.getGeneralCatalogFiles()) {
                        General_CatalogFile_dto generalCatalogFileDto = generalCatalogFileService.convertInventoryEquipmentFileDto(generalCatalogFile);
                        generalCatalogFileDtos.add(generalCatalogFileDto);
                    }
                    List<AssetHistory_All> sortedHistoryList = updateSucuess.getAssetHistoryList()
                            .stream()
                            .sorted((h1, h2) -> h2.getDate().compareTo(h1.getDate())) // 從新到舊
                            .collect(Collectors.toList());
                    List<AssetHistoryDto_All> assetHistoryDtoAlls = new ArrayList<>();
                    for (AssetHistory_All assetHistoryAll : sortedHistoryList) {
                        AssetHistoryDto_All assetHistoryDtoAll = assetHistoryAllService.convert_AssetHistoryDto(assetHistoryAll);
                        assetHistoryDtoAlls.add(assetHistoryDtoAll);
                    }
                    generalCatalogDto.setAssetHistoryDtoAlls(assetHistoryDtoAlls);
                    generalCatalogDto.setGeneralCatalogFileDtos(generalCatalogFileDtos);
                    response.put("code", 200);
                    response.put("message", "success");
                    data.put("inventoryEquipmentDto", generalCatalogDto);
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


    @DeleteMapping("/asset_manage/delete_image")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> inventory_delete_file(@RequestParam String fileId, @RequestParam String final_property_id) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        try {
            //刪除檔案方法放在service
            inventoryEquipmentFileService.deleteFile(fileId);
            Inventory_Equipment inventoryequipment = assetITService.getPropertyDetails(final_property_id);
            List<InventoryEquipmentFileDto> inventoryEquipmentFileDtos = new ArrayList<>();
            for (InventoryEquipmentFile inventoryEquipmentFile : inventoryequipment.getInventoryEquipmentFiles()) {
                InventoryEquipmentFileDto inventoryEquipmentFileDto = inventoryEquipmentFileService.convertInventoryEquipmentFileDto(inventoryEquipmentFile);
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

    @DeleteMapping("/asset_manage/delete_image_All")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> inventory_delete_file_All(@RequestParam String fileId, @RequestParam String asset_id) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        try {
            //刪除檔案方法放在service
            generalCatalogFileService.deleteFile(fileId);
            General_Catalog generalCatalog = assetAllService.getPropertyDetails(asset_id);
            List<General_CatalogFile_dto> generalCatalogFileDtos = new ArrayList<>();
            for (GeneralCatalogFile generalCatalogFile : generalCatalog.getGeneralCatalogFiles()) {
                General_CatalogFile_dto generalCatalogFileDto = generalCatalogFileService.convertInventoryEquipmentFileDto(generalCatalogFile);
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
