package org.chyunn_web.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.chyunn_web.bean.*;
import org.chyunn_web.dto.*;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class AssetController_All {
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    LocationService locationService;
    @Autowired
    Asset_All_Service assetAllService;
    @Autowired
    AssetHistory_ALL_Service assetHistoryAllService;
    @Autowired
    General_CatalogFile_Service generalCatalogFileService;
    @Autowired
    RandomSampling_All_Service randomSamplingAllService;

    //轉去總明細
    @GetMapping("/asset/select_All_Asset")
    public String into_select_ITAsset(Model model) {
        model.addAttribute("locations", locationService.findAll());
        return "assetManagement/select_All_Asset";
    }


    //查找所有事件
    @PostMapping("/asset/select_All_TAsset")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> select_incident(
            @RequestParam(defaultValue = "0") int page, // 頁數
            @RequestParam(defaultValue = "100") int size // 每頁顯示多少筆資料
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<General_Catalog> generalCatalogPage = assetAllService.findAll(pageable);

            List<General_CatalogDto> generalCatalogDtos = new ArrayList<>();
            for (General_Catalog generalCatalog : generalCatalogPage.getContent()) {
                General_CatalogDto dto = assetAllService.convertInventory_EquipmentDto(generalCatalog);
                generalCatalogDtos.add(dto);
            }

            response.put("content", generalCatalogDtos);
            response.put("totalPages", generalCatalogPage.getTotalPages());
            response.put("totalElements", generalCatalogPage.getTotalElements());
            response.put("currentPage", page);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); // 可改為 log.error
            response.put("message", "查詢資料發生錯誤");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/asset/search_general_catalog")
    public ResponseEntity<Map<String, Object>> get_search(
            @RequestParam String final_property_id) {

        Map<String, Object> response = new HashMap<>();
        try {
            List<General_Catalog> assetPage = assetAllService.getProperty(final_property_id);
            List<General_CatalogDto> generalCatalogDtos = new ArrayList<>();
            for (General_Catalog generalCatalog : assetPage) {
                General_CatalogDto dto = assetAllService.convertInventory_EquipmentDto(generalCatalog);
                generalCatalogDtos.add(dto);
            }
            response.put("code", 200);
            response.put("data", generalCatalogDtos); // 返回查詢結果
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); // 或改成 log.error("查詢錯誤", e);
            response.put("code", 500);
            response.put("message", "查詢資料失敗！");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/asset/filterLocation_general_catalog")
    public ResponseEntity<Map<String, Object>> filterLocation(
            @RequestParam String location
    ) {
        Map<String, Object> response = new HashMap<>();
        try {

            List<General_Catalog> assetPage = assetAllService.filterLocation(location);

            List<General_CatalogDto> generalCatalogDtos = new ArrayList<>();
            for (General_Catalog generalCatalog : assetPage) {
                General_CatalogDto dto = assetAllService.convertInventory_EquipmentDto(generalCatalog);
                generalCatalogDtos.add(dto);
            }
            response.put("code", 200);
            response.put("data", generalCatalogDtos); // 返回查詢結果
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); // 或改成 log.error("查詢錯誤", e);
            response.put("code", 500);
            response.put("message", "查詢資料失敗！");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/asset_manage/addProperty_All")
    public ResponseEntity<Map<String, Object>> addProperty(@RequestBody General_Catalog generalCatalog) {
        Map<String, Object> response = new HashMap<>();
        if (generalCatalog.getAsset_id() == null || generalCatalog.getAsset_id().trim().isEmpty()) {
            response.put("code", 400);
            response.put("message", "財產編號不得為空！");
            return ResponseEntity.badRequest().body(response);
        }

        if (generalCatalog.getStorage_location() == null || generalCatalog.getStorage_location().trim().isEmpty()) {
            response.put("code", 400);
            response.put("message", "放置處不得為空！");
            return ResponseEntity.badRequest().body(response);
        }
        try {
            General_Catalog new_generalCatalog = assetAllService.saveProperty(generalCatalog);
            response.put("code", 200);
            response.put("message", "新增成功");
            response.put("data", new_generalCatalog);
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

    @PostMapping("asset/getPropertyDetails_All")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPropertyDetails(@RequestParam String id) {
        Map<String, Object> response = new HashMap<>();
        try {

            General_Catalog generalCatalog = assetAllService.getPropertyDetails(id);
            General_CatalogDto generalCatalogDto = assetAllService.convertInventory_EquipmentDto(generalCatalog);
            List<AssetHistory_All> sortedHistoryList = generalCatalog.getAssetHistoryList()
                    .stream()
                    .sorted((h1, h2) -> h2.getDate().compareTo(h1.getDate())) // 從新到舊
                    .collect(Collectors.toList());
            List<AssetHistoryDto_All> assetHistoryDtoAlls = new ArrayList<>();
            for (AssetHistory_All assetHistoryAll : sortedHistoryList) {
                AssetHistoryDto_All assetHistoryDtoAll = assetHistoryAllService.convert_AssetHistoryDto(assetHistoryAll);
                assetHistoryDtoAlls.add(assetHistoryDtoAll);
            }
            List<General_CatalogFile_dto> generalCatalogFileDtos = new ArrayList<>();
            for (GeneralCatalogFile generalCatalogFile : generalCatalog.getGeneralCatalogFiles()) {
                General_CatalogFile_dto generalCatalogFileDto = generalCatalogFileService.convertInventoryEquipmentFileDto(generalCatalogFile);
                generalCatalogFileDto.setAsset_id(generalCatalogFile.getGeneralCatalog().getAsset_id());
                generalCatalogFileDtos.add(generalCatalogFileDto);
            }
            generalCatalogDto.setAssetHistoryDtoAlls(assetHistoryDtoAlls);
            generalCatalogDto.setGeneralCatalogFileDtos(generalCatalogFileDtos);
            response.put("code", 200);
            response.put("data", generalCatalogDto); // 財產內容
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

    @PostMapping("/asset_manage/updateProperty_All")
    public ResponseEntity<Map<String, Object>> updateProperty_All(
            @RequestParam String asset_id,
            @RequestParam String name,
            @RequestParam String category,
            @RequestParam String storage_location,
            @RequestParam String custodian,
            @RequestParam String contact,
            @RequestParam String purchase_date,
            @RequestParam String purchase_price,
            @RequestParam String tax,
            @RequestParam String supplier,
            @RequestParam String serial_or_size,
            @RequestParam String quantity_or_unit,
            @RequestParam String model,
            @RequestParam String change_log,
            @RequestParam Boolean update_history

    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            General_Catalog generalCatalog = assetAllService.getPropertyDetails(asset_id);
            if (generalCatalog == null) {
                throw new RuntimeException("找不到指定的財產資料");
            }

            generalCatalog.setName(name);
            generalCatalog.setCategory(category);
            generalCatalog.setStorage_location(storage_location);
            generalCatalog.setCustodian(custodian);
            generalCatalog.setContact(contact);
            generalCatalog.setPurchase_date(purchase_date);
            generalCatalog.setPurchase_price(purchase_price);
            generalCatalog.setTax(tax);
            generalCatalog.setSupplier(supplier);
            generalCatalog.setSerial_or_size(serial_or_size);
            generalCatalog.setQuantity_or_unit(quantity_or_unit);
            generalCatalog.setModel(model);
            generalCatalog.setChange_log(change_log);
            // If update_history is true, create a new history record and add it to the list
            if (update_history) {
                AssetHistory_All assetHistoryAll = new AssetHistory_All();
                assetHistoryAll.setDate(new SimpleDateFormat("yyyy/MM/dd").format(new Date()));
                assetHistoryAll.setCustodian(custodian);
                assetHistoryAll.setLocation(storage_location);
                assetHistoryAll.setChange_record(change_log);
                assetHistoryAll.setGeneralCatalog(generalCatalog);

                generalCatalog.getAssetHistoryList().add(assetHistoryAll);
            }
            assetAllService.updateProperty(generalCatalog);
            General_CatalogDto generalCatalogDto = assetAllService.convertInventory_EquipmentDto(generalCatalog);
            response.put("code", 200);
            response.put("message", "success");
            response.put("inventoryEquipment", generalCatalogDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("code", 500);
            response.put("message", "更新錯誤");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

    }

    @PostMapping("/asset/random-inventory-All")
    public ResponseEntity<Map<String, Object>> generateRandomInventory(@RequestBody Map<String, Integer> request) {
        int count = request.getOrDefault("count", 50);
        Map<String, Object> response = new HashMap<>();
        System.out.println(count);
        try {
            // 先檢查是否還有正在進行的盤點
            List<RandomSampling_All> inProgress = randomSamplingAllService.findByRandomSampling();
            if (!inProgress.isEmpty()) {
                response.put("code", 409);
                response.put("message", "仍有尚未完成的盤點項目，請先完成後再重新產生");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            // 取得所有尚未被選過或已完成的資料（以便重新輪次）
            List<General_Catalog> candidates = assetAllService.findByInventoryCheckedFalseAndInventoryInProgressFalse();

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
            List<General_Catalog> selected = candidates.stream()
                    .limit(count)
                    .sorted(Comparator.comparing(General_Catalog::getStorage_location, Comparator.nullsLast(String::compareTo)))
                    .toList();
            for (General_Catalog e : selected) {
                RandomSampling_All randomSamplingAll = new RandomSampling_All();
                randomSamplingAll.setAsset_id(e.getAsset_id());
                randomSamplingAll.setState(false);
                randomSamplingAll.setInventory_in_progress(true);
                randomSamplingAllService.saveRandomSampling(randomSamplingAll);
                e.setInventory_in_progress(true);                // 盤點中
                e.setState(false);                              // 尚未完成
            }
            assetAllService.saveInventoryData(selected);
            response.put("code", 200);
            response.put("message", "隨機盤點資料產生成功，");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "伺服器錯誤");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/asset/random-inventory-download-all")
    public void downloadRandomInventory(HttpServletResponse response) throws IOException {
        try {
            List<String> ids = randomSamplingAllService.findIdsByStateFalse();
            List<General_Catalog> selected = assetAllService.findByRandomSampling_All_ServiceAll(ids);
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
                    "財產編號", "類別", "名稱", "型號", "尺寸/序號", "數量/單位", "購買日期", "購入廠商",
                    "購買金額", "稅金", "放置處", "保管人", "聯繫窗口", "異動記錄"
            };
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(cellStyle);
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
            for (General_Catalog item : selected) {
                Row row = sheet.createRow(rowIdx++);
                String[] values = {
                        item.getAsset_id(), item.getCategory(), item.getName(),
                        item.getModel(), item.getSerial_or_size(), item.getQuantity_or_unit(), String.valueOf(item.getPurchase_date())
                        ,item.getSupplier(), item.getPurchase_price(),
                        item.getTax(), item.getStorage_location(), item.getCustodian(), item.getContact(), item.getChange_log()
                };
                for (int i = 0; i < values.length; i++) {
                    Cell cell = row.createCell(i);
                    cell.setCellValue(values[i]);
                    cell.setCellStyle(cellStyle);
                }
            }
            // Set the response header with proper file name encoding
            String fileName = "總目錄隨機盤點資料_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
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
    @PostMapping("/asset/uncompleted-inventory-all")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uncompleted_inventory() {

        Map<String, Object> response = new HashMap<>();
        try {
            List<String> ids = randomSamplingAllService.findIdsByStateFalse();
            List<General_Catalog> selected = assetAllService.findByRandomSampling_All_ServiceAll(ids);
            List<General_CatalogDto> generalCatalogDtos = new ArrayList<>();
            for (General_Catalog generalCatalog : selected) {
                if (!generalCatalog.getState() && generalCatalog.getInventory_in_progress()) {
                    General_CatalogDto dto = assetAllService.convertInventory_EquipmentDto(generalCatalog);
                    generalCatalogDtos.add(dto);
                }
            }
            response.put("data", generalCatalogDtos); // 返回查詢結果
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "伺服器錯誤");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    //更新抽查清單狀態-已盤點
    @PutMapping("/asset/uncompleted_inventory_update_All")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uncompleted_inventory_update(@RequestParam String asset_id) {

        Map<String, Object> response = new HashMap<>();
        try {
            General_Catalog generalCatalog = assetAllService.getPropertyDetails(asset_id);
            RandomSampling_All randomSamplingAll = randomSamplingAllService.getRandomSampling_All(asset_id);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String today = LocalDate.now().format(formatter);
            if (!generalCatalog.getState() && generalCatalog.getInventory_in_progress()) {
                generalCatalog.setInventory_in_progress(false);
                generalCatalog.setState(true);
                generalCatalog.setInventory_date(today);
                randomSamplingAll.setInventory_in_progress(false);
                randomSamplingAll.setState(true);
            } else {
                generalCatalog.setInventory_in_progress(true);
                generalCatalog.setState(false);
                generalCatalog.setInventory_date(today);
                generalCatalog.setInventory_date(null);
                randomSamplingAll.setInventory_in_progress(true);
                randomSamplingAll.setState(false);
            }
            assetAllService.updateProperty(generalCatalog);
            response.put("inventory_in_progress", generalCatalog.getInventory_in_progress());
            response.put("code", 200);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "伺服器錯誤");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/asset/resetInventory_All")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resetInventory() {
        Map<String, Object> response = new HashMap<>();
        try {
            assetAllService.resetInventory();
            response.put("message", "重製成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); // 可選：記錄日誌
            response.put("message", "重製失敗：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/asset/exportExcel_Asset")
    public void exportExcel(HttpServletResponse response) throws IOException {
        try {
            List<General_Catalog> data = assetAllService.getAll(); // 可以替換成篩選條件
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = URLEncoder.encode("財產清單總目錄.xlsx", StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("財產清單_總目錄");

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setWrapText(true);
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);

            Row header = sheet.createRow(0);
            String[] columns = {
                    "財產編號", "類別", "名稱", "型號", "尺寸/序號", "數量/單位", "購買日期", "購入廠商",
                    "購買金額", "稅金", "放置處", "保管人", "聯繫窗口", "異動記錄"
            };
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(cellStyle);
            }

//            for (int i = 6; i <= 11; i++) {
//                sheet.setColumnHidden(i, true);
//            }

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
            for (General_Catalog item : data) {
                Row row = sheet.createRow(rowIdx++);
                String[] values = {
                        item.getAsset_id(), item.getCategory(), item.getName(),
                        item.getModel(), item.getSerial_or_size(), item.getQuantity_or_unit(), String.valueOf(item.getPurchase_date())
                        ,item.getSupplier(), item.getPurchase_price(),
                        item.getTax(), item.getStorage_location(), item.getCustodian(), item.getContact(), item.getChange_log()
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

    @GetMapping("/asset/excel/general_catalog")
    @ResponseBody
    public String excel_general_catalog_inster_datebase() {
        String excelFilePath = "\\\\192.168.2.3\\群運共用夾\\※資訊部專用\\※資訊物品盤點清冊\\※財產目錄總表.xls";
        try (FileInputStream fis = new FileInputStream(new File(excelFilePath));
             Workbook workbook = new HSSFWorkbook(fis)) {

            // 獲取第2個工作表（索引從 0 開始，所以 1 是第2個工作表）
            Sheet sheet = workbook.getSheetAt(1);
            if (sheet == null) {
                System.out.println("lost NO.2 ");
                return null;
            }

            // 用於存儲抓取的資料
            List<General_Catalog> generalCatalogList = new ArrayList<>();

            // 迭代每一行，從第二行開始
            for (int rowIndex = 1; rowIndex < sheet.getPhysicalNumberOfRows(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) { // 避免 NullPointerException
                    System.out.println("跳過空白行: " + rowIndex);
                    continue; // 跳過此行
                }
                // 創建一個新的 general_catalog 物件
                General_Catalog generalCatalog = new General_Catalog();

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
                                    // 真的被格式成 Excel 日期時才轉換
                                    Date date = cell.getDateCellValue();
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                                    cellValue = sdf.format(date);
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
                            generalCatalog.setAsset_id(cellValue); // 預設空字串
                            break;
                        case 1:
                            generalCatalog.setCategory(cellValue); // 預設空字串
                            break;
                        case 2:
                            generalCatalog.setName(cellValue); // 預設空字串
                            break;
                        case 3:
                            generalCatalog.setModel(cellValue); // 預設空字串
                            break;
                        case 4:
                            generalCatalog.setSerial_or_size(cellValue); // 如果空則為 false
                            break;
                        case 5:
                            generalCatalog.setQuantity_or_unit(cellValue); // 預設空字串
                            break;
                        case 6:
                            generalCatalog.setPurchase_date(cellValue); // 預設空字串
                            break;
                        case 7:
                            generalCatalog.setSupplier(cellValue); // 預設空字串
                            break;
                        case 8:
                                generalCatalog.setPurchase_price(cellValue);
                            break;
                        case 9:
                            generalCatalog.setTax(cellValue); // 預設空字串
                            break;
                        case 10:
                            generalCatalog.setStorage_location(cellValue); // 預設空字串
                            break;
                        case 11:
                            generalCatalog.setCustodian(cellValue); // 預設空字串
                            break;
                        case 12:
                            generalCatalog.setContact(cellValue); // 預設空字串
                            break;
                        case 13:
                            generalCatalog.setChange_log(cellValue);// 預設空字串
                            break;
                        default:
                            break;
                    }
                }
                // 把每一行資料存入 list 中
                generalCatalogList.add(generalCatalog);
            }
            for (General_Catalog item : generalCatalogList) {
                System.out.println(item.toString());
            }
            assetAllService.insertGeneralCatalogList(generalCatalogList);

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
