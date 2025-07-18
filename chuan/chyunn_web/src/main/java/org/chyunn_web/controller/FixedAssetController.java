package org.chyunn_web.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.chyunn_web.bean.Asset.*;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class FixedAssetController {
    @Autowired
    FixedAssetService fixedAssetService;
    @Autowired
    LocationService locationService;
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    AssetHistoryService assetHistoryService;
    @Autowired
    FixedAssetFileService fixedAssetFileService;


    //轉去資訊設備總表
    @GetMapping("/asset/select_FixedAsset")
    public String into_select_ITAsset(Model model) {
        model.addAttribute("locations", locationService.findAllFixedAssetLocation());
        return "assetManagement/select_FixedAsset";
    }

    //查找所有事件
    @PostMapping("/asset/select_FixedAsset")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> select_incident(
            @RequestParam(defaultValue = "0") int page, // 頁數
            @RequestParam(defaultValue = "100") int size // 每頁顯示多少筆資料
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<FixedAsset> fixedAssetPage = fixedAssetService.findAll(pageable);

            List<FixedAssetDto> fixedAssetDtos = new ArrayList<>();
            for (FixedAsset fixedAsset : fixedAssetPage.getContent()) {
                FixedAssetDto fixedAssetDto = fixedAssetService.convertFixedAsset(fixedAsset);
                fixedAssetDtos.add(fixedAssetDto);
            }

            response.put("content", fixedAssetDtos);
            response.put("totalPages", fixedAssetPage.getTotalPages());
            response.put("totalElements", fixedAssetPage.getTotalElements());
            response.put("currentPage", page);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); // 可改為 log.error
            response.put("message", "查詢資料發生錯誤");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/asset_manage/addFixedAsset")
    public ResponseEntity<Map<String, Object>> addProperty( @RequestParam("major_category_id") String majorCategoryId,
                                                            @RequestParam("major_category_name") String majorCategoryName,
                                                            @RequestParam("sub_category_id") String subCategoryId,
                                                            @RequestParam("sub_category_name") String subCategoryName,
                                                            @RequestParam("fixed_location") String fixedLocation,
                                                            @RequestParam("unit") String unit,
                                                            @RequestParam("acquisition_date") String acquisitionDate,
                                                            @RequestParam("useful_life_original") Integer usefulLifeOriginal,
                                                            @RequestParam("useful_life_new") Integer usefulLifeNew,
                                                            @RequestParam("useful_life_converted") Integer usefulLifeConverted,
                                                            @RequestParam("depreciation_end_date") String depreciationEndDate,
                                                            @RequestParam("remark") String remark,
                                                            @RequestParam("quantity") Integer quantity,
                                                            @RequestParam("acquisition_cost") String acquisitionCost,
                                                            @RequestParam("improvement_cost") String improvementCost,
                                                            @RequestParam("salvage_value") String salvageValue,
                                                            @RequestParam("net_acquisition_cost") String netAcquisitionCost,
                                                            @RequestParam("current_period_depreciation") String currentPeriodDepreciation,
                                                            @RequestParam("accumulated_depreciation") String accumulatedDepreciation,
                                                            @RequestParam("net_book_value") String netBookValue,
                                                            @RequestParam("improvement_serial_no") String improvementSerialNo,
                                                            @RequestParam("equipment_name") String equipmentName,
                                                            @RequestParam("catalog_major_name") String catalogMajorName,
                                                            @RequestParam("asset_id") String assetId,
                                                            @RequestParam("custodian_id") String custodianId,
                                                            @RequestParam("custodian_name") String custodianName,
                                                            @RequestParam("brand_id") String brandId,
                                                            @RequestParam("brand_name") String brandName,
                                                            @RequestParam("vendor_id") String vendorId,
                                                            @RequestParam("vendor_name") String vendorName,
                                                            @RequestParam("voucher_type") String voucherType,
                                                            @RequestParam("is_depreciation_recalc_excluded") String isDepRecalcExcluded,
                                                            @RequestParam("is_transferred_to_b_account") String isTransferredToB,
                                                            @RequestParam("department_id") String departmentId,
                                                            @RequestParam("barcode") String barcode,
                                                            @RequestParam("asset_attribute") String assetAttribute,
                                                            @RequestParam("asset_category") String assetCategory,
                                                            @RequestParam("depreciation_method") String depreciationMethod,
                                                            @RequestParam(value = "files", required = false) MultipartFile[] files) {
        Map<String, Object> response = new HashMap<>();
        FixedAsset fixedAsset = new FixedAsset();
        fixedAsset.setMajor_category_id(majorCategoryId);
        fixedAsset.setMajor_category_name(majorCategoryName);
        fixedAsset.setSub_category_id(subCategoryId);
        fixedAsset.setSub_category_name(subCategoryName);
        fixedAsset.setFixed_location(fixedLocation);
        fixedAsset.setUnit(unit);
        fixedAsset.setAcquisition_date(acquisitionDate);
        fixedAsset.setUseful_life_original(usefulLifeOriginal);
        fixedAsset.setUseful_life_new(usefulLifeNew);
        fixedAsset.setUseful_life_converted(usefulLifeConverted);
        fixedAsset.setDepreciation_end_date(depreciationEndDate);
        fixedAsset.setRemark(remark);
        fixedAsset.setQuantity(quantity);
        fixedAsset.setAcquisition_cost(acquisitionCost);
        fixedAsset.setImprovement_cost(improvementCost);
        fixedAsset.setSalvage_value(salvageValue);
        fixedAsset.setNet_acquisition_cost(netAcquisitionCost);
        fixedAsset.setCurrent_period_depreciation(currentPeriodDepreciation);
        fixedAsset.setAccumulated_depreciation(accumulatedDepreciation);
        fixedAsset.setNet_book_value(netBookValue);
        fixedAsset.setImprovement_serial_no(improvementSerialNo);
        fixedAsset.setEquipment_name(equipmentName);
        fixedAsset.setCatalog_major_name(catalogMajorName);
        fixedAsset.setAsset_id(assetId);
        fixedAsset.setCustodian_id(custodianId);
        fixedAsset.setCustodian_name(custodianName);
        fixedAsset.setBrand_id(brandId);
        fixedAsset.setBrand_name(brandName);
        fixedAsset.setVendor_id(vendorId);
        fixedAsset.setVendor_name(vendorName);
        fixedAsset.setVoucher_type(voucherType);
        fixedAsset.setIs_depreciation_recalc_excluded(isDepRecalcExcluded);
        fixedAsset.setIs_transferred_to_b_account(isTransferredToB);
        fixedAsset.setDepartment_id(departmentId);
        fixedAsset.setBarcode(barcode);
        fixedAsset.setAsset_attribute(assetAttribute);
        fixedAsset.setAsset_category(assetCategory);
        fixedAsset.setDepreciation_method(depreciationMethod);
        if (subCategoryId == null || subCategoryId.trim().isEmpty()) {
            response.put("code", 400);
            response.put("message", "財產細項編號不得為空！");
            return ResponseEntity.badRequest().body(response);
        }
        if (fixedLocation == null || fixedLocation.trim().isEmpty()) {
            response.put("code", 400);
            response.put("message", "放置處不得為空！");
            return ResponseEntity.badRequest().body(response);
        }
        if (fixedAsset != null) {
            // 上傳檔案（若有）
            List<FixedAssetFile> fixedAssetFiles = new ArrayList<>();
            // 用來追蹤已儲存的臨時檔案
            List<File> tempFiles = new ArrayList<>();
            String uploadDirBase = "\\\\192.168.2.3\\群運共用夾\\※資訊部專用\\沈世泉\\inventory_file\\";
            String eventFolderPath = uploadDirBase + "fixedAsset_file_" + fixedAsset.getSub_category_id();
            File dir = new File(eventFolderPath);
            // 嘗試存儲檔案
            if (files != null ) {
                if (!dir.exists() && !dir.mkdirs()) {
                    response.put("code", 500);
                    response.put("message", "無法建立附件存放目錄");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
                if ( files.length > 10) {
                    response.put("code", 400);
                    response.put("message", "該設備最多只能儲存 10 張圖片");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }

                try {
                    // 儲存檔案並創建檔案記錄
                    for (MultipartFile file : files) {
                        if (!file.isEmpty()) {
                            String originalFilename = file.getOriginalFilename();
                            if (originalFilename != null) {
                                // 儲存檔案到事件資料夾
                                UUID uuid = UUID.randomUUID();
                                String filePath = eventFolderPath + File.separator + uuid + ".jpg";
                                File storedFile = new File(filePath);
                                file.transferTo(storedFile);
                                tempFiles.add(storedFile);  // 將已儲存的檔案加入列表

                                FixedAssetFile fixedAssetFile = new FixedAssetFile();
                                fixedAssetFile.setFileId(String.valueOf(uuid));
                                fixedAssetFile.setFileName(uuid + ".jpg");
                                fixedAssetFiles.add(fixedAssetFile);
                            }
                        }
                    }
                } catch (IOException e) {
                    response.put("code", 500);
                    response.put("message", "檔案上傳失敗");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
            }
            // 有檔案則存入
            if (!fixedAssetFiles.isEmpty()) {
                fixedAsset.getFixedAssetFiles().addAll(fixedAssetFiles);
            }
        }

        try {
            FixedAsset new_fixedAsset = fixedAssetService.insertfixedAsseAndFiles(fixedAsset,fixedAsset.getFixedAssetFiles());
            FixedAssetDto dto = fixedAssetService.convertFixedAsset(new_fixedAsset);
            dto.setImage_Number(String.valueOf(new_fixedAsset.getFixedAssetFiles().size()));
            response.put("code", 200);
            response.put("message", "新增成功");
            response.put("data", dto);
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

    @PostMapping("asset/getFixedAssetDetails")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPropertyDetails(@RequestParam String id) {
        Map<String, Object> response = new HashMap<>();
        try {

            FixedAsset fixedAsset = fixedAssetService.getFixedAssetDetails(id);
            FixedAssetDto fixedAssetDto = fixedAssetService.convertFixedAsset(fixedAsset);
            List<FixedAssetHistory> sortedHistoryList = fixedAsset.getFixedAssetHistories()
                    .stream()
                    .sorted((h1, h2) -> h2.getDate().compareTo(h1.getDate())) // 從新到舊
                    .collect(Collectors.toList());
            List<FixedAssetHistoryDto> fixedAssetHistoryDtos = new ArrayList<>();
            for (FixedAssetHistory fixedAssetHistory : sortedHistoryList) {
                FixedAssetHistoryDto fixedAssetHistoryDto = assetHistoryService.convert_FixedAssetHistory(fixedAssetHistory);
                fixedAssetHistoryDtos.add(fixedAssetHistoryDto);
            }
            List<FixedAssetFileDto> fixedAssetFileDtos = new ArrayList<>();
            for (FixedAssetFile fixedAssetFile : fixedAsset.getFixedAssetFiles()) {
                FixedAssetFileDto fixedAssetFileDto = fixedAssetFileService.converFixedAssetFile(fixedAssetFile);
                fixedAssetFileDto.setSub_category_id(fixedAssetFile.getFixedAsset().getSub_category_id());
                fixedAssetFileDtos.add(fixedAssetFileDto);
            }
            fixedAssetDto.setFixedAssetHistoryDtoList(fixedAssetHistoryDtos);
            fixedAssetDto.setFixedAssetFileDtoList(fixedAssetFileDtos);
            response.put("code", 200);
            response.put("data", fixedAssetDto); // 財產內容
            response.put("locations", locationService.findAllFixedAssetLocation()); // 所有放置處

            return ResponseEntity.ok(response); // 返回成功的 response
        } catch (Exception e) {
            e.printStackTrace(); // 可替換為 log.error("取得財產細節失敗", e);
            response.put("code", 500);
            response.put("message", "失敗");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/asset/searchFixedAsset")
    public ResponseEntity<Map<String, Object>> get_search(
            @RequestParam String sub_category_id) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<FixedAsset> fixedAssets = fixedAssetService.getProperty(sub_category_id);
            List<FixedAssetDto> fixedAssetDtos = new ArrayList<>();
            for (FixedAsset fixedAsset : fixedAssets) {
                FixedAssetDto dto = fixedAssetService.convertFixedAsset(fixedAsset);
                fixedAssetDtos.add(dto);
            }
            response.put("code", 200);
            response.put("data", fixedAssetDtos); // 返回查詢結果
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); // 或改成 log.error("查詢錯誤", e);
            response.put("code", 500);
            response.put("message", "查詢資料失敗！");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/asset/FixedAsset_exportExcel")
    public void exportExcel(HttpServletResponse response) throws IOException {
        try {
            List<FixedAsset> data = fixedAssetService.getAll(); // 可以替換成篩選條件
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = URLEncoder.encode("財務_固定資產.xlsx", StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("財務_固定資產");

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setWrapText(true);
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);

            Row header = sheet.createRow(0);
            String[] columns = {
                    "財產大類編號", "財產大類名稱", "財產細項編號", "財產細項名稱", "所在地", "單位", "取得時間",
                    "耐用年數-原表", "耐用年數-新表", "耐用年數-換算", "截止提列日期", "備註", "數量",
                    "取得原價", "改良或修理", "預留殘值", "取得原價減預留殘值", "本期提列數", "截至本期累計數",
                    "未折減餘額", "改良修理序號", "設備或生財器具名稱", "財產目錄大類名稱", "ASFA004C",
                    "保管人", "保管人名稱", "廠牌代號", "廠牌名稱", "供應商代號", "廠商名稱", "固資傳票類別",
                    "不重算註記", "轉B帳註記", "部門\\工地編號", "條碼代號", "資產屬性", "固資分類", "折舊方法(中文)"
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
                sheet.setColumnWidth(i, 30 * 256);
//                if (!sheet.isColumnHidden(i)) {
//                    if (i == 4 || i == 5) {
//                        sheet.setColumnWidth(i, 70 * 256);
//                    } else if (i == 15) {
//                        sheet.setColumnWidth(i, 30 * 256);
//                    } else {
//                        sheet.setColumnWidth(i, 15 * 256);
//                    }
//                }
            }

            int rowIdx = 1;
            for (FixedAsset item : data) {
                Row row = sheet.createRow(rowIdx++);
                String[] values = {
                        item.getMajor_category_id(),
                        item.getMajor_category_name(),
                        item.getSub_category_id(),
                        item.getSub_category_name(),
                        item.getFixed_location(),
                        item.getUnit(),
                        item.getAcquisition_date(),
                        String.valueOf(item.getUseful_life_original()),
                        String.valueOf(item.getUseful_life_new()),
                        String.valueOf(item.getUseful_life_converted()),
                        item.getDepreciation_end_date(),
                        item.getRemark(),
                        String.valueOf(item.getQuantity()),
                        item.getAcquisition_cost(),
                        item.getImprovement_cost(),
                        item.getSalvage_value(),
                        item.getNet_acquisition_cost(),
                        item.getCurrent_period_depreciation(),
                        item.getAccumulated_depreciation(),
                        item.getNet_book_value(),
                        item.getImprovement_serial_no(),
                        item.getEquipment_name(),
                        item.getCatalog_major_name(),
                        item.getAsset_id(),
                        item.getCustodian_id(),
                        item.getCustodian_name(),
                        item.getBrand_id(),
                        item.getBrand_name(),
                        item.getVendor_id(),
                        item.getVendor_name(),
                        item.getVoucher_type(),
                        item.getIs_depreciation_recalc_excluded(),
                        item.getIs_transferred_to_b_account(),
                        item.getDepartment_id(),
                        item.getBarcode(),
                        item.getAsset_attribute(),
                        item.getAsset_category(),
                        item.getDepreciation_method()
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

    @GetMapping("/asset/filterFixedAssetLocation")
    public ResponseEntity<Map<String, Object>> filterLocation(
            @RequestParam String location

    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<FixedAsset> fixedAssets = fixedAssetService.filterLocation(location);
            List<FixedAssetDto> fixedAssetDtos = new ArrayList<>();
            for (FixedAsset fixedAsset : fixedAssets) {
                FixedAssetDto dto = fixedAssetService.convertFixedAsset(fixedAsset);
                fixedAssetDtos.add(dto);
            }
            response.put("code", 200);
            response.put("data", fixedAssetDtos); // 返回查詢結果
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); // 或改成 log.error("查詢錯誤", e);
            response.put("code", 500);
            response.put("message", "查詢資料失敗！");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/asset_manage/updateFixedAsset")
    public ResponseEntity<Map<String, Object>> updateProperty(
            @RequestParam("major_category_id") String majorCategoryId,
            @RequestParam("major_category_name") String majorCategoryName,
            @RequestParam("sub_category_id") String subCategoryId,
            @RequestParam("sub_category_name") String subCategoryName,
            @RequestParam("fixed_location") String fixedLocation,
            @RequestParam("unit") String unit,
            @RequestParam("acquisition_date") String acquisitionDate,
            @RequestParam("useful_life_original") Integer usefulLifeOriginal,
            @RequestParam("useful_life_new") Integer usefulLifeNew,
            @RequestParam("useful_life_converted") Integer usefulLifeConverted,
            @RequestParam("depreciation_end_date") String depreciationEndDate,
            @RequestParam("remark") String remark,
            @RequestParam("quantity") Integer quantity,
            @RequestParam("acquisition_cost") String acquisitionCost,
            @RequestParam("improvement_cost") String improvementCost,
            @RequestParam("salvage_value") String salvageValue,
            @RequestParam("net_acquisition_cost") String netAcquisitionCost,
            @RequestParam("current_period_depreciation") String currentPeriodDepreciation,
            @RequestParam("accumulated_depreciation") String accumulatedDepreciation,
            @RequestParam("net_book_value") String netBookValue,
            @RequestParam("improvement_serial_no") String improvementSerialNo,
            @RequestParam("equipment_name") String equipmentName,
            @RequestParam("catalog_major_name") String catalogMajorName,
            @RequestParam("asset_id") String assetId,
            @RequestParam("custodian_id") String custodianId,
            @RequestParam("custodian_name") String custodianName,
            @RequestParam("brand_id") String brandId,
            @RequestParam("brand_name") String brandName,
            @RequestParam("vendor_id") String vendorId,
            @RequestParam("vendor_name") String vendorName,
            @RequestParam("voucher_type") String voucherType,
            @RequestParam("is_depreciation_recalc_excluded") String isDepRecalcExcluded,
            @RequestParam("is_transferred_to_b_account") String isTransferredToB,
            @RequestParam("department_id") String departmentId,
            @RequestParam("barcode") String barcode,
            @RequestParam("asset_attribute") String assetAttribute,
            @RequestParam("asset_category") String assetCategory,
            @RequestParam("depreciation_method") String depreciationMethod,
            @RequestParam("update_history") boolean update_history,
            @RequestParam(value = "files", required = false) MultipartFile[] files

    ) {
        Map<String, Object> response = new HashMap<>();
        try {

            FixedAsset fixedAsset = fixedAssetService.getFixedAssetDetails(subCategoryId);
            if (fixedAsset == null) {
                throw new RuntimeException("找不到指定的財產資料");
            }
            List<FixedAssetFile> existingFiles = fixedAsset.getFixedAssetFiles();
            int existingFileCount = existingFiles.size();  // 目前已存在的圖片數量
            if (fixedAsset != null) {
                // 上傳檔案（若有）
                List<FixedAssetFile> fixedAssetFiles = new ArrayList<>();
                // 用來追蹤已儲存的臨時檔案
                List<File> tempFiles = new ArrayList<>();
                String uploadDirBase = "\\\\192.168.2.3\\群運共用夾\\※資訊部專用\\沈世泉\\inventory_file\\";
                String eventFolderPath = uploadDirBase + "fixedAsset_file_" + fixedAsset.getSub_category_id();
                File dir = new File(eventFolderPath);
                // 嘗試存儲檔案
                if (files != null ) {
                    if (!dir.exists() && !dir.mkdirs()) {
                        response.put("code", 500);
                        response.put("message", "無法建立附件存放目錄");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                    }
                    if (existingFileCount + files.length > 10) {
                        response.put("code", 400);
                        response.put("message", "該設備最多只能儲存 10 張圖片，目前已有 " + existingFileCount + " 張，新增的圖片數量超過限制");
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                    }

                    try {
                        // 儲存檔案並創建檔案記錄
                        for (MultipartFile file : files) {
                            if (!file.isEmpty()) {
                                String originalFilename = file.getOriginalFilename();
                                if (originalFilename != null) {
                                    // 儲存檔案到事件資料夾
                                    UUID uuid = UUID.randomUUID();
                                    String filePath = eventFolderPath + File.separator + uuid + ".jpg";
                                    File storedFile = new File(filePath);
                                    file.transferTo(storedFile);
                                    tempFiles.add(storedFile);  // 將已儲存的檔案加入列表

                                    FixedAssetFile fixedAssetFile = new FixedAssetFile();
                                    fixedAssetFile.setFileId(String.valueOf(uuid));
                                    fixedAssetFile.setFileName(uuid + ".jpg");
                                    fixedAssetFiles.add(fixedAssetFile);
                                }
                            }
                        }
                    } catch (IOException e) {
                        response.put("code", 500);
                        response.put("message", "檔案上傳失敗");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                    }
                }
                // 若有新檔案則存入
                if (!fixedAssetFiles.isEmpty()) {
                    fixedAsset.getFixedAssetFiles().addAll(fixedAssetFiles);
                }
            }

            fixedAsset.setMajor_category_id(majorCategoryId);
            fixedAsset.setMajor_category_name(majorCategoryName);
            fixedAsset.setSub_category_id(subCategoryId);
            fixedAsset.setSub_category_name(subCategoryName);
            fixedAsset.setFixed_location(fixedLocation);
            fixedAsset.setUnit(unit);
            fixedAsset.setAcquisition_date(acquisitionDate);
            fixedAsset.setUseful_life_original(usefulLifeOriginal);
            fixedAsset.setUseful_life_new(usefulLifeNew);
            fixedAsset.setUseful_life_converted(usefulLifeConverted);
            fixedAsset.setDepreciation_end_date(depreciationEndDate);
            fixedAsset.setRemark(remark);
            fixedAsset.setQuantity(quantity);
            fixedAsset.setAcquisition_cost(acquisitionCost);
            fixedAsset.setImprovement_cost(improvementCost);
            fixedAsset.setSalvage_value(salvageValue);
            fixedAsset.setNet_acquisition_cost(netAcquisitionCost);
            fixedAsset.setCurrent_period_depreciation(currentPeriodDepreciation);
            fixedAsset.setAccumulated_depreciation(accumulatedDepreciation);
            fixedAsset.setNet_book_value(netBookValue);
            fixedAsset.setImprovement_serial_no(improvementSerialNo);
            fixedAsset.setEquipment_name(equipmentName);
            fixedAsset.setCatalog_major_name(catalogMajorName);
            fixedAsset.setAsset_id(assetId);
            fixedAsset.setCustodian_id(custodianId);
            fixedAsset.setCustodian_name(custodianName);
            fixedAsset.setBrand_id(brandId);
            fixedAsset.setBrand_name(brandName);
            fixedAsset.setVendor_id(vendorId);
            fixedAsset.setVendor_name(vendorName);
            fixedAsset.setVoucher_type(voucherType);
            fixedAsset.setIs_depreciation_recalc_excluded(isDepRecalcExcluded);
            fixedAsset.setIs_transferred_to_b_account(isTransferredToB);
            fixedAsset.setDepartment_id(departmentId);
            fixedAsset.setBarcode(barcode);
            fixedAsset.setAsset_attribute(assetAttribute);
            fixedAsset.setAsset_category(assetCategory);
            fixedAsset.setDepreciation_method(depreciationMethod);

            // If update_history is true, create a new history record and add it to the list
            if (update_history) {
                FixedAssetHistory fixedAssetHistory = new FixedAssetHistory();
                fixedAssetHistory.setDate(new SimpleDateFormat("yyyy/MM/dd").format(new Date()));
                fixedAssetHistory.setCustodian(custodianName);
                fixedAssetHistory.setFixed_location(fixedLocation);
                fixedAssetHistory.setChange_record(remark);
                fixedAssetHistory.setFixedAsset(fixedAsset);

                fixedAsset.getFixedAssetHistories().add(fixedAssetHistory);
            }
            fixedAssetService.insertfixedAsseAndFiles(fixedAsset,fixedAsset.getFixedAssetFiles());

            FixedAssetDto fixedAssetDto = fixedAssetService.convertFixedAsset(fixedAsset);

            response.put("code", 200);
            response.put("message", "success");
            response.put("fixedAssetDto", fixedAssetDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("code", 500);
            response.put("message", "更新錯誤");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

    }



    @GetMapping("/asset/FixedAssetLocation")
    public ResponseEntity<Map<String, Object>> get_locations() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("code", 200);
            response.put("data", locationService.findAllFixedAssetLocation());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("code", 500);
            response.put("message", "查詢位置資料時發生錯誤");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/asset_manage/delete_image_FixedAsset")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> inventory_delete_file(@RequestParam String fileId, @RequestParam String sub_category_id) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        try {
            //刪除檔案方法放在service
            fixedAssetFileService.deleteFile(fileId);
            FixedAsset fixedAsset = fixedAssetService.getFixedAssetDetails(sub_category_id);
            List<FixedAssetFileDto> fixedAssetFileDtos = new ArrayList<>();
            for (FixedAssetFile fixedAssetFile : fixedAsset.getFixedAssetFiles()) {
                FixedAssetFileDto fixedAssetFileDto = fixedAssetFileService.converFixedAssetFile(fixedAssetFile);
                fixedAssetFileDtos.add(fixedAssetFileDto);
            }
            data.put("fixedAssetFileDtos", fixedAssetFileDtos);
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

    @GetMapping("/asset_manage/excel/fixedAsset")
    @ResponseBody
    public String excel_inster_datebase() {
        String excelFilePath = "\\\\192.168.2.3\\群運共用夾\\※資訊部專用\\※資訊物品盤點清冊\\1140630財產目錄.xlsx";

        try (FileInputStream fis = new FileInputStream(new File(excelFilePath));
             Workbook workbook = new XSSFWorkbook(fis)) {

            // 獲取第0個工作表（索引從 0 開始，所以 0 是第1個工作表）
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                System.out.println("lost NO.0 ");
                return null;
            }

            // 用於存儲抓取的資料
            List<FixedAsset> fixedAssetList = new ArrayList<>();

            // 迭代每一行，從第二行開始
            for (int rowIndex = 1; rowIndex < sheet.getPhysicalNumberOfRows(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) { // 避免 NullPointerException
                    System.out.println("跳過空白行: " + rowIndex);
                    continue; // 跳過此行
                }
                // 創建一個新的 fixedAsset 物件
                FixedAsset fixedAsset = new FixedAsset();

                // 迭代每列（A-R 對應列索引 0-37）
                for (int colIndex = 0; colIndex < 38; colIndex++) {

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

                    switch (colIndex) {
                        case 0:
                            fixedAsset.setMajor_category_id(cellValue); // 財產大類編號
                            break;
                        case 1:
                            fixedAsset.setMajor_category_name(cellValue); // 財產大類名稱
                            break;
                        case 2:
                            fixedAsset.setSub_category_id(cellValue); // 財產細項編號
                            break;
                        case 3:
                            fixedAsset.setSub_category_name(cellValue); // 財產細項名稱
                            break;
                        case 4:
                            fixedAsset.setFixed_location(cellValue); // 所在地
                            break;
                        case 5:
                            fixedAsset.setUnit(cellValue); // 單位
                            break;
                        case 6:
                            fixedAsset.setAcquisition_date(cellValue); // 取得時間
                            break;
                        case 7:
                            fixedAsset.setUseful_life_original(Integer.parseInt(cellValue)); // 耐用年數-原表
                            break;
                        case 8:
                            fixedAsset.setUseful_life_new(Integer.parseInt(cellValue)); // 耐用年數-新表
                            break;
                        case 9:
                            fixedAsset.setUseful_life_converted(Integer.parseInt(cellValue)); // 耐用年數-換算
                            break;
                        case 10:
                            fixedAsset.setDepreciation_end_date(cellValue); // 截止提列日期
                            break;
                        case 11:
                            fixedAsset.setRemark(cellValue); // 備註
                            break;
                        case 12:
                            fixedAsset.setQuantity(Integer.parseInt(cellValue)); // 數量
                            break;
                        case 13:
                            fixedAsset.setAcquisition_cost(cellValue); // 取得原價
                            break;
                        case 14:
                            fixedAsset.setImprovement_cost(cellValue); // 改良或修理
                            break;
                        case 15:
                            fixedAsset.setSalvage_value(cellValue); // 預留殘值
                            break;
                        case 16:
                            fixedAsset.setNet_acquisition_cost(cellValue); // 取得原價減預留殘值
                            break;
                        case 17:
                            fixedAsset.setCurrent_period_depreciation(cellValue); // 本期提列數
                            break;
                        case 18:
                            fixedAsset.setAccumulated_depreciation(cellValue); // 截至本期累計數
                            break;
                        case 19:
                            fixedAsset.setNet_book_value(cellValue); // 未折減餘額
                            break;
                        case 20:
                            fixedAsset.setImprovement_serial_no(cellValue); // 改良修理序號
                            break;
                        case 21:
                            fixedAsset.setEquipment_name(cellValue); // 設備或生財器具名稱
                            break;
                        case 22:
                            fixedAsset.setCatalog_major_name(cellValue); // 財產目錄大類名稱
                            break;
                        case 23:
                            fixedAsset.setAsset_id(cellValue); // ASFA004C
                            break;
                        case 24:
                            fixedAsset.setCustodian_id(cellValue); // 保管人
                            break;
                        case 25:
                            fixedAsset.setCustodian_name(cellValue); // 保管人名稱
                            break;
                        case 26:
                            fixedAsset.setBrand_id(cellValue); // 廠牌代號
                            break;
                        case 27:
                            fixedAsset.setBrand_name(cellValue); // 廠牌名稱
                            break;
                        case 28:
                            fixedAsset.setVendor_id(cellValue); // 供應商代號
                            break;
                        case 29:
                            fixedAsset.setVendor_name(cellValue); // 廠商名稱
                            break;
                        case 30:
                            fixedAsset.setVoucher_type(cellValue); // 固資傳票類別
                            break;
                        case 31:
                            fixedAsset.setIs_depreciation_recalc_excluded(cellValue); // 不重算註記
                            break;
                        case 32:
                            fixedAsset.setIs_transferred_to_b_account(cellValue); // 轉B帳註記
                            break;
                        case 33:
                            fixedAsset.setDepartment_id(cellValue); // 部門\工地編號
                            break;
                        case 34:
                            fixedAsset.setBarcode(cellValue); // 條碼代號
                            break;
                        case 35:
                            fixedAsset.setAsset_attribute(cellValue); // 資產屬性
                            break;
                        case 36:
                            fixedAsset.setAsset_category(cellValue); // 固資分類
                            break;
                        case 37:
                            fixedAsset.setDepreciation_method(cellValue); // 折舊方法
                            break;
                        default:
                            break;
                    }
                }
                // 把每一行資料存入 list 中
                fixedAssetList.add(fixedAsset);
            }
            fixedAssetService.insertFixedAsset(fixedAssetList);

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
            model.addAttribute("uri", request.getRequestURI());
            model.addAttribute("userName", userName);
            model.addAttribute("userRoles", roles);
            model.addAttribute("creator", userName);
        }
    }
}
