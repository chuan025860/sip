package org.chyunn_web.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.chyunn_web.bean.Asset.AssetHistory;
import org.chyunn_web.bean.Asset.InventoryEquipmentFile;
import org.chyunn_web.bean.Asset.Inventory_Equipment;
import org.chyunn_web.bean.Asset.RandomSampling_IT;
import org.chyunn_web.dto.AssetHistoryDto;
import org.chyunn_web.dto.InventoryEquipmentFileDto;
import org.chyunn_web.dto.Inventory_EquipmentDto;
import org.chyunn_web.security.JwtTokenProvider;
import org.chyunn_web.service.*;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class AssetController_IT {
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    Asset_IT_Service assetITService;
    @Autowired
    LocationService locationService;
    @Autowired
    AssetHistoryService assetHistoryService;
    @Autowired
    InventoryEquipmentFileService inventoryEquipmentFileService;
    @Autowired
    RandomSampling_IT_Service randomSampling_IT_Service;

    //轉去資訊設備總表
    @GetMapping("/asset/select_ITAsset")
    public String into_select_ITAsset(Model model) {
        model.addAttribute("locations", locationService.findAll());
        return "assetManagement/select_ITAsset";
    }

    //查找所有事件
    @PostMapping("/asset/select_ITAsset")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> select_incident(
            @RequestParam(defaultValue = "0") int page, // 頁數
            @RequestParam(defaultValue = "100") int size // 每頁顯示多少筆資料
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Inventory_Equipment> inventoryEquipmentPage = assetITService.findAll(pageable);

            List<Inventory_EquipmentDto> inventoryEquipmentDtos = new ArrayList<>();
            for (Inventory_Equipment inventoryEquipment : inventoryEquipmentPage.getContent()) {
                Inventory_EquipmentDto dto = assetITService.convertInventory_EquipmentDto(inventoryEquipment);
                inventoryEquipmentDtos.add(dto);
            }

            response.put("content", inventoryEquipmentDtos);
            response.put("totalPages", inventoryEquipmentPage.getTotalPages());
            response.put("totalElements", inventoryEquipmentPage.getTotalElements());
            response.put("currentPage", page);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); // 可改為 log.error
            response.put("message", "查詢資料發生錯誤");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @PostMapping("asset/getPropertyDetails")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPropertyDetails(@RequestParam String id) {
        Map<String, Object> response = new HashMap<>();
        try {

            Inventory_Equipment inventoryEquipment = assetITService.getPropertyDetails(id);
            Inventory_EquipmentDto inventoryEquipmentDto = assetITService.convertInventory_EquipmentDto(inventoryEquipment);
            List<AssetHistory> sortedHistoryList = inventoryEquipment.getAssetHistoryList()
                    .stream()
                    .sorted((h1, h2) -> h2.getDate().compareTo(h1.getDate())) // 從新到舊
                    .collect(Collectors.toList());
            List<AssetHistoryDto> assetHistoryDtos = new ArrayList<>();
            for (AssetHistory assetHistory : sortedHistoryList) {
                AssetHistoryDto assetHistoryDto = assetHistoryService.convert_AssetHistoryDto(assetHistory);
                assetHistoryDtos.add(assetHistoryDto);
            }
            List<InventoryEquipmentFileDto> inventoryEquipmentFileDtoList = new ArrayList<>();
            for (InventoryEquipmentFile inventoryEquipmentFile : inventoryEquipment.getInventoryEquipmentFiles()) {
                InventoryEquipmentFileDto inventoryEquipmentFileDto = inventoryEquipmentFileService.convertInventoryEquipmentFileDto(inventoryEquipmentFile);
                inventoryEquipmentFileDto.setFinal_property_id(inventoryEquipmentFile.getInventoryEquipment().getFinal_property_id());
                inventoryEquipmentFileDtoList.add(inventoryEquipmentFileDto);
            }
            inventoryEquipmentDto.setAssetHistoryDtoList(assetHistoryDtos);
            inventoryEquipmentDto.setInventoryEquipmentFileDtoList(inventoryEquipmentFileDtoList);
            response.put("code", 200);
            response.put("data", inventoryEquipmentDto); // 財產內容
            response.put("locations", locationService.findAll()); // 所有放置處

            return ResponseEntity.ok(response); // 返回成功的 response
        } catch (Exception e) {
            e.printStackTrace(); // 可替換為 log.error("取得財產細節失敗", e);
            response.put("code", 500);
            response.put("message", "失敗");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/asset_manage/updateProperty")
    public ResponseEntity<Map<String, Object>> updateProperty(
            @RequestParam String final_property_id,
            @RequestParam String name,
            @RequestParam String category,
            @RequestParam String location,
            @RequestParam String custodian,
            @RequestParam String contact_window,
            @RequestParam String purchase_date,
            @RequestParam String purchase_amount,
            @RequestParam String tax,
            @RequestParam String supplier,
            @RequestParam String size_serial,
            @RequestParam String quantity_unit,
            @RequestParam String remarks,
            @RequestParam String change_record,
            @RequestParam Boolean update_history

    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Inventory_Equipment inventoryEquipment = assetITService.getPropertyDetails(final_property_id);
            if (inventoryEquipment == null) {
                throw new RuntimeException("找不到指定的財產資料");
            }

            inventoryEquipment.setName(name);
            inventoryEquipment.setCategory(category);
            inventoryEquipment.setLocation(location);
            inventoryEquipment.setCustodian(custodian);
            inventoryEquipment.setContact_window(contact_window);
            inventoryEquipment.setPurchase_date(purchase_date);
            inventoryEquipment.setPurchase_amount(purchase_amount);
            inventoryEquipment.setTax(tax);
            inventoryEquipment.setSupplier(supplier);
            inventoryEquipment.setSize_serial(size_serial);
            inventoryEquipment.setQuantity_unit(quantity_unit);
            inventoryEquipment.setRemarks(remarks);
            inventoryEquipment.setChange_record(change_record);
            // If update_history is true, create a new history record and add it to the list
            if (update_history) {
                AssetHistory assetHistory = new AssetHistory();
                assetHistory.setDate(new SimpleDateFormat("yyyy/MM/dd").format(new Date()));
                assetHistory.setCustodian(custodian);
                assetHistory.setLocation(location);
                assetHistory.setChange_record(change_record);
                assetHistory.setInventoryEquipment(inventoryEquipment);

                inventoryEquipment.getAssetHistoryList().add(assetHistory);
            }
            assetITService.updateProperty(inventoryEquipment);

            Inventory_EquipmentDto inventoryEquipmentDto = assetITService.convertInventory_EquipmentDto(inventoryEquipment);

            response.put("code", 200);
            response.put("message", "success");
            response.put("inventoryEquipment", inventoryEquipmentDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("code", 500);
            response.put("message", "更新錯誤");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

    }

    @GetMapping("/asset/locations")
    public ResponseEntity<Map<String, Object>> get_locations() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("code", 200);
            response.put("data", locationService.findAll());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("code", 500);
            response.put("message", "查詢位置資料時發生錯誤");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @PostMapping("/asset_manage/addProperty")
    public ResponseEntity<Map<String, Object>> addProperty(@RequestBody Inventory_Equipment inventoryEquipment) {
        Map<String, Object> response = new HashMap<>();
        if (inventoryEquipment.getFinal_property_id() == null || inventoryEquipment.getFinal_property_id().trim().isEmpty()) {
            response.put("code", 400);
            response.put("message", "最終財產編號不得為空！");
            return ResponseEntity.badRequest().body(response);
        }

        if (inventoryEquipment.getLocation() == null || inventoryEquipment.getLocation().trim().isEmpty()) {
            response.put("code", 400);
            response.put("message", "放置處不得為空！");
            return ResponseEntity.badRequest().body(response);
        }
        try {
            Inventory_Equipment new_inventoryEquipment = assetITService.saveProperty(inventoryEquipment);
            response.put("code", 200);
            response.put("message", "新增成功");
            response.put("data", new_inventoryEquipment);
            return ResponseEntity.ok(response);
        } catch (DataIntegrityViolationException e) {
            response.put("code", 409); // 409 Conflict
            response.put("message", "主鍵重複，該財產編號已存在！");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (ConstraintViolationException e) {
            response.put("code", 400); // Bad request
            response.put("message", "欄位驗證失敗：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("code", 500);
            response.put("message", "查詢位置資料時發生錯誤");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/asset/search")
    public ResponseEntity<Map<String, Object>> get_search(
            @RequestParam String final_property_id) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Inventory_Equipment> assetPage = assetITService.getProperty(final_property_id);
            List<Inventory_EquipmentDto> inventoryEquipmentDtos = new ArrayList<>();
            for (Inventory_Equipment inventoryEquipment : assetPage) {
                Inventory_EquipmentDto dto = assetITService.convertInventory_EquipmentDto(inventoryEquipment);
                inventoryEquipmentDtos.add(dto);
            }
            response.put("code", 200);
            response.put("data", inventoryEquipmentDtos); // 返回查詢結果
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); // 或改成 log.error("查詢錯誤", e);
            response.put("code", 500);
            response.put("message", "查詢資料失敗！");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/asset/filterLocation")
    public ResponseEntity<Map<String, Object>> filterLocation(
            @RequestParam String location

    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Inventory_Equipment> assetPage = assetITService.filterLocation(location);
            List<Inventory_EquipmentDto> inventoryEquipmentDtos = new ArrayList<>();
            for (Inventory_Equipment inventoryEquipment : assetPage) {
                Inventory_EquipmentDto dto = assetITService.convertInventory_EquipmentDto(inventoryEquipment);
                inventoryEquipmentDtos.add(dto);
            }
            response.put("code", 200);
            response.put("data", inventoryEquipmentDtos); // 返回查詢結果
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); // 或改成 log.error("查詢錯誤", e);
            response.put("code", 500);
            response.put("message", "查詢資料失敗！");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/asset/exportExcel")
    public void exportExcel(HttpServletResponse response) throws IOException {
        try {
            List<Inventory_Equipment> data = assetITService.getAll(); // 可以替換成篩選條件
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = URLEncoder.encode("財產清單資訊.xlsx", StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("財產清單_資訊");

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setWrapText(true);
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);

            Row header = sheet.createRow(0);
            String[] columns = {
                    "財產編號", "列管編號", "最終財產編號", "類別", "名稱", "型號/備註", "尺寸/序號", "數量/單位", "購買日期", "購入廠商",
                    "購買金額", "稅金", "放置處", "保管人", "聯繫窗口", "異動記錄"
            };
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(cellStyle);
            }

            for (int i = 6; i <= 11; i++) {
                sheet.setColumnHidden(i, true);
            }

            int totalColumns = columns.length;
            for (int i = 0; i < totalColumns; i++) {
                if (!sheet.isColumnHidden(i)) {
                    if (i == 4 || i == 5) {
                        sheet.setColumnWidth(i, 70 * 256);
                    } else if (i == 15) {
                        sheet.setColumnWidth(i, 30 * 256);
                    } else {
                        sheet.setColumnWidth(i, 15 * 256);
                    }
                }
            }

            int rowIdx = 1;
            for (Inventory_Equipment item : data) {
                Row row = sheet.createRow(rowIdx++);
                String[] values = {
                        item.getProperty_id(), item.getManagement_id(), item.getFinal_property_id(), item.getCategory(),
                        item.getName(), item.getRemarks(), item.getSize_serial(), item.getQuantity_unit(),
                        String.valueOf(item.getPurchase_date()), item.getSupplier(), item.getPurchase_amount(),
                        item.getTax(), item.getLocation(), item.getCustodian(), item.getContact_window(), item.getChange_record()
                };
                for (int i = 0; i < values.length; i++) {
                    Cell cell = row.createCell(i);
                    cell.setCellValue(values[i]);
                    cell.setCellStyle(cellStyle);
                }
            }

            workbook.write(response.getOutputStream());
            workbook.close();

        } catch (Exception e) {
            // 錯誤處理：回應 500 錯誤狀態與訊息
            try {
                e.printStackTrace();
                response.setContentType("text/plain; charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("匯出 Excel 失敗，請稍後再試");
            } catch (IOException ioException) {
                ioException.printStackTrace(); // 最終備援，記錄日誌
            }
        }
    }

    @PostMapping("/asset/random-inventory")
    public ResponseEntity<Map<String, Object>> generateRandomInventory(@RequestBody Map<String, Integer> request) {
        int count = request.getOrDefault("count", 50);
        Map<String, Object> response = new HashMap<>();
        System.out.println(count);
        try {
            // 先檢查是否還有正在進行的盤點
            List<RandomSampling_IT> inProgress = randomSampling_IT_Service.findByRandomSampling();
            if (!inProgress.isEmpty()) {
                response.put("code", 409);
                response.put("message", "仍有尚未完成的盤點項目，請先完成後再重新產生");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            // 取得所有尚未被選過或已完成的資料（以便重新輪次）
            List<Inventory_Equipment> candidates = assetITService.findByInventoryCheckedFalseAndInventoryInProgressFalse();

            if (candidates.size() == 0) {
                response.put("code", 410);
                response.put("message", "目前沒有可供盤點的項目，是否要開始新一輪盤點？");
                return ResponseEntity.status(HttpStatus.GONE).body(response);
            }

            // 若數量不足，就提示目前剩餘可盤點的數量
            if (candidates.size() < count) {
                response.put("code", 400);
                response.put("message", "剩餘可盤點數量不足，目前僅剩 " + candidates.size() + " 筆可供選擇，請先完成當前輪次或減少數量後重試。");
                response.put("remaining", candidates.size());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            // 隨機選取 count 筆資料
            //Collections.shuffle(candidates); 隨機演算法（Fisher-Yates 洗牌演算法）打亂這個 List 的順序。
            Collections.shuffle(candidates);
            //從 candidates 這個 List 中取出前 count 筆元素，並收集成新的 List，賦值給 selected
            List<Inventory_Equipment> selected = candidates.stream()
                    .limit(count)
                    .sorted(Comparator.comparing(Inventory_Equipment::getLocation, Comparator.nullsLast(String::compareTo)))
                    .toList();
            for (Inventory_Equipment e : selected) {
                System.out.println(e.getLocation());
                RandomSampling_IT randomSamplingIt = new RandomSampling_IT();
                randomSamplingIt.setFinal_property_id(e.getFinal_property_id());
                randomSamplingIt.setState(false);
                randomSamplingIt.setInventory_in_progress(true);
                randomSampling_IT_Service.saveRandomSampling(randomSamplingIt);
                e.setInventory_in_progress(true);                // 盤點中
                e.setState(false);                              // 尚未完成
            }
            assetITService.saveInventoryData(selected);
            response.put("code", 200);
            response.put("message", "隨機盤點資料產生成功，");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "伺服器錯誤");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/asset/random-inventory-download")
    public void downloadRandomInventory(HttpServletResponse response) throws IOException {
        try {
            List<String> ids = randomSampling_IT_Service.findIdsByStateFalse();
            List<Inventory_Equipment> selected = assetITService.findByRandomSampling_IT_ServiceIT(ids);
            // 生成 Excel 文件
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("隨機盤點資料");

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setWrapText(true);
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);

            // 設置標題行
            Row header = sheet.createRow(0);
            String[] columns = {
                    "財產編號", "列管編號", "最終財產編號", "類別", "名稱", "型號/備註", "尺寸/序號", "數量/單位", "購買日期", "購入廠商",
                    "購買金額", "稅金", "放置處", "保管人", "聯繫窗口", "異動記錄"
            };
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(cellStyle);
            }

            for (int i = 6; i <= 11; i++) {
                sheet.setColumnHidden(i, true);
            }

            int totalColumns = columns.length;
            for (int i = 0; i < totalColumns; i++) {
                if (!sheet.isColumnHidden(i)) {
                    if (i == 4 || i == 5) {
                        sheet.setColumnWidth(i, 70 * 256);
                    } else if (i == 15) {
                        sheet.setColumnWidth(i, 30 * 256);
                    } else {
                        sheet.setColumnWidth(i, 15 * 256);
                    }
                }
            }
            int rowIdx = 1;
            for (Inventory_Equipment item : selected) {
                Row row = sheet.createRow(rowIdx++);
                String[] values = {
                        item.getProperty_id(), item.getManagement_id(), item.getFinal_property_id(), item.getCategory(),
                        item.getName(), item.getRemarks(), item.getSize_serial(), item.getQuantity_unit(),
                        String.valueOf(item.getPurchase_date()), item.getSupplier(), item.getPurchase_amount(),
                        item.getTax(), item.getLocation(), item.getCustodian(), item.getContact_window(), item.getChange_record()
                };
                for (int i = 0; i < values.length; i++) {
                    Cell cell = row.createCell(i);
                    cell.setCellValue(values[i]);
                    cell.setCellStyle(cellStyle);
                }
            }
            // Set the response header with proper file name encoding
            String fileName = "隨機盤點資料_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(fileName, "UTF-8").replace("+", "%20") + "\"");

            // Write the workbook to the response output stream
            workbook.write(response.getOutputStream());
            workbook.close();

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("下載失敗");
        }
    }

    //抽查清單
    @PostMapping("/asset/uncompleted-inventory")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uncompleted_inventory() {

        Map<String, Object> response = new HashMap<>();
        try {
            List<String> ids = randomSampling_IT_Service.findIdsByStateFalse();
            List<Inventory_Equipment> selected = assetITService.findByRandomSampling_IT_ServiceIT(ids);
            List<Inventory_EquipmentDto> inventoryEquipmentDtos = new ArrayList<>();
            for (Inventory_Equipment inventoryEquipment : selected) {
                if (!inventoryEquipment.getState() && inventoryEquipment.getInventory_in_progress()) {
                    Inventory_EquipmentDto dto = assetITService.convertInventory_EquipmentDto(inventoryEquipment);
                    inventoryEquipmentDtos.add(dto);
                }
            }
            response.put("data", inventoryEquipmentDtos); // 返回查詢結果
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "伺服器錯誤");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    //更新抽查清單狀態-已盤點
    @PutMapping("/asset/uncompleted_inventory_update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uncompleted_inventory_update(@RequestParam String final_property_id) {

        Map<String, Object> response = new HashMap<>();
        try {
            Inventory_Equipment inventoryEquipment = assetITService.getPropertyDetails(final_property_id);
            RandomSampling_IT randomSamplingIt = randomSampling_IT_Service.getRandomSampling_IT(final_property_id);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String today = LocalDate.now().format(formatter);
            if (!inventoryEquipment.getState() && inventoryEquipment.getInventory_in_progress()) {
                inventoryEquipment.setInventory_in_progress(false);
                inventoryEquipment.setState(true);
                inventoryEquipment.setInventory_date(today);
                randomSamplingIt.setInventory_in_progress(false);
                randomSamplingIt.setState(true);
            } else {
                inventoryEquipment.setInventory_in_progress(true);
                inventoryEquipment.setState(false);
                inventoryEquipment.setInventory_date(today);
                inventoryEquipment.setInventory_date(null);
                randomSamplingIt.setInventory_in_progress(true);
                randomSamplingIt.setState(false);
            }
            assetITService.updateProperty(inventoryEquipment);
            response.put("inventory_in_progress", inventoryEquipment.getInventory_in_progress());
            response.put("code", 200);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "伺服器錯誤");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/asset/resetInventory")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resetInventory() {
        Map<String, Object> response = new HashMap<>();
        try {
            assetITService.resetInventory();
            response.put("message", "重製成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); // 可選：記錄日誌
            response.put("message", "重製失敗：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @GetMapping("/inventory/excel/inventory_equipment")
    @ResponseBody
    public String excel_inventory_equipment_inster_datebase() {
        String excelFilePath = "\\\\192.168.2.3\\群運共用夾\\※資訊部專用\\※資訊物品盤點清冊\\※財產目錄總表.xls";

        try (FileInputStream fis = new FileInputStream(new File(excelFilePath));
             Workbook workbook = new HSSFWorkbook(fis)) {

            // 獲取第6個工作表（索引從 0 開始，所以 5 是第6個工作表）
            Sheet sheet = workbook.getSheetAt(5);
            if (sheet == null) {
                System.out.println("lost NO.5 ");
                return null;
            }

            // 用於存儲抓取的資料
            List<Inventory_Equipment> inventoryequipmentList = new ArrayList<>();

            // 迭代每一行，從第二行開始
            for (int rowIndex = 1; rowIndex < sheet.getPhysicalNumberOfRows(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) { // 避免 NullPointerException
                    System.out.println("跳過空白行: " + rowIndex);
                    continue; // 跳過此行
                }
                // 創建一個新的 Inventory 物件
                Inventory_Equipment inventoryequipment = new Inventory_Equipment();

                // 迭代每列（A-R 對應列索引 0-17）
                for (int colIndex = 0; colIndex < 18; colIndex++) {

                    Cell cell = row.getCell(colIndex);
                    String cellValue = "";

                    if (cell != null) {
                        switch (cell.getCellType()) {
                            case STRING:
                                cellValue = cell.getStringCellValue();
                                break;
                            case NUMERIC:
                                if (DateUtil.isCellDateFormatted(cell)) {
                                    Date date = cell.getDateCellValue();
                                    System.out.println("原始日期 (Excel): " + cell.getNumericCellValue());
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                                    cellValue = sdf.format(date);
                                    System.out.println("轉換後的日期: " + cellValue);
                                } else {
                                    // 如果只是純數字就直接轉字串
                                    cellValue = String.format("%.0f", cell.getNumericCellValue());
                                }
                                break;
                            default:
                                cellValue = ""; // 預設為空字串
                                break;
                        }
                    }

                    // 根據欄位索引設置相應的欄位值
                    switch (colIndex) {
                        case 0:
                            inventoryequipment.setProperty_id(cellValue); // 預設空字串
                            break;
                        case 1:
                            inventoryequipment.setManagement_id(cellValue); // 預設空字串
                            break;
                        case 2:
                            inventoryequipment.setFinal_property_id(cellValue); // 預設空字串
                            break;

                        case 3:
                            inventoryequipment.setCategory(cellValue); // 預設空字串
                            break;
                        case 4:
                            inventoryequipment.setName(cellValue); // 預設空字串
                            break;
                        case 5:
                            inventoryequipment.setRemarks(cellValue); // 預設空字串
                            break;
                        case 6:
                            inventoryequipment.setSize_serial(cellValue); // 預設空字串
                            break;
                        case 7:
                            System.out.println(cellValue);
                            inventoryequipment.setQuantity_unit(cellValue); // 預設空字串
                            break;
                        case 8:
                            inventoryequipment.setPurchase_date(cellValue); // 預設空字串
                            break;
                        case 9:
                            inventoryequipment.setSupplier(cellValue); // 預設空字串
                            break;
                        case 10:
                            inventoryequipment.setPurchase_amount(cellValue);
                            break;
                        case 11:
                            inventoryequipment.setTax(cellValue);// 預設空字串
                            break;
                        case 12:
                            inventoryequipment.setLocation(cellValue); // 預設空字串
                            break;
                        case 13:
                            inventoryequipment.setCustodian(cellValue); // 預設空字串
                            break;
                        case 14:
                            inventoryequipment.setContact_window(cellValue); // 預設空字串
                            break;
                        case 15:
                            inventoryequipment.setChange_record(cellValue); // 預設空字串
                            break;
                        default:
                            break;
                    }
                }
                // 把每一行資料存入 list 中
                inventoryequipmentList.add(inventoryequipment);
            }
            assetITService.insertInventoryList(inventoryequipmentList);

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @ModelAttribute
    public void addUserInfoToModel(HttpServletRequest request, Model model) {
        String token = null;

        // 從 Cookie 中取得 token
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("authToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        if (token != null && jwtTokenProvider.validateToken(token)) {
            String userName = jwtTokenProvider.getUserNameFromToken(token);
            List<String> roles = jwtTokenProvider.getUserRolesFromToken(token);

            model.addAttribute("userName", userName);
            model.addAttribute("userRoles", roles);
            model.addAttribute("creator", userName);
        }
    }
}
