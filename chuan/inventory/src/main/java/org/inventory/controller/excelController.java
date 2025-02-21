package org.inventory.controller;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.inventory.bean.General_Catalog;
import org.inventory.bean.Inventory_Equipment;
import org.inventory.service.GeneralCatalogService;
import org.inventory.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Controller
public class excelController {

    @Autowired
    InventoryService inventoryService;
    @Autowired
    GeneralCatalogService generalCatalogService;

    @GetMapping("/inventory/excel/inventory_equipment")
    @ResponseBody
    public String excel_inventory_equipment_inster_datebase() {
        String excelFilePath = "C:\\Users\\gagood72\\Desktop\\chuan\\※財產目錄總表0205.xls";
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
                                // 將數字轉為字串，避免科學記數法
                                cellValue = String.format("%.0f", cell.getNumericCellValue());
                                break;
                            default:
                                cellValue = ""; // 預設為空字串
                                break;
                        }
                    }

                    // 根據欄位索引設置相應的欄位值
                    switch (colIndex) {
                        case 0:
                            inventoryequipment.setAudit_status(cellValue); // 預設空字串
                            break;
                        case 1:
                            inventoryequipment.setProperty_id(cellValue); // 預設空字串
                            break;
                        case 2:
                            inventoryequipment.setManagement_id(cellValue); // 預設空字串
                            break;
                        case 3:
                            inventoryequipment.setFinal_property_id(cellValue); // 預設空字串
                            break;
                        case 4:
                            inventoryequipment.setScrapped(cellValue); // 如果空則為 false
                            break;
                        case 5:
                            inventoryequipment.setCategory(cellValue); // 預設空字串
                            break;
                        case 6:
                            inventoryequipment.setName(cellValue); // 預設空字串
                            break;
                        case 7:
                            inventoryequipment.setRemarks(cellValue); // 預設空字串
                            break;
                        case 8:
                            inventoryequipment.setSize_serial(cellValue); // 預設空字串
                            break;
                        case 9:
                            inventoryequipment.setQuantity_unit(cellValue); // 預設空字串
                            break;
                        case 10:
                            inventoryequipment.setPurchase_date(cellValue); // 預設空字串
                            break;
                        case 11:
                            inventoryequipment.setSupplier(cellValue); // 預設空字串
                            break;
                        case 12:
                            if (cellValue != null && !cellValue.isEmpty()) {
                                inventoryequipment.setPurchase_amount(Integer.valueOf(cellValue));
                            } else {
                                inventoryequipment.setPurchase_amount(null);
                            }
                            break;
                        case 13:
                            inventoryequipment.setTax(cellValue);// 預設空字串
                            break;
                        case 14:
                            inventoryequipment.setLocation(cellValue); // 預設空字串
                            break;
                        case 15:
                            inventoryequipment.setCustodian(cellValue); // 預設空字串
                            break;
                        case 16:
                            inventoryequipment.setContact_window(cellValue); // 預設空字串
                            break;
                        case 17:
                            inventoryequipment.setChange_record(cellValue); // 預設空字串
                            break;
                        default:
                            break;
                    }
                }
                // 把每一行資料存入 list 中
                inventoryequipmentList.add(inventoryequipment);
            }
            inventoryService.insertInventoryList(inventoryequipmentList);

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @GetMapping("/inventory/excel/general_catalog")
    @ResponseBody
    public String excel_general_catalog_inster_datebase() {
        String excelFilePath = "C:\\Users\\gagood72\\Desktop\\chuan\\※財產目錄總表0205.xls";
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
                                // 將數字轉為字串，避免科學記數法
                                cellValue = String.format("%.0f", cell.getNumericCellValue());
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
                            if (cellValue.isEmpty()) {
                                generalCatalog.setPurchase_price(null);
                            } else {
                                generalCatalog.setPurchase_price(Integer.valueOf(cellValue));
                            }
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
            generalCatalogService.insertGeneralCatalogList(generalCatalogList);

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @GetMapping("/downloadInventory")
    public ResponseEntity<byte[]> downloadInventory() throws IOException {
        List<Inventory_Equipment> inventoryequipmentList = inventoryService.findAll(); // 抓出所有資料

        // 創建 Excel 工作簿
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Inventory");

        // 設定欄寬為 15.25 (15.25 * 256 = 3904)
        // 設定每個欄位的欄寬
        sheet.setColumnWidth(0, 4096); // 設定欄寬為 15.25 (對應第一欄)
        sheet.setColumnWidth(1, 9251); // 設定欄寬為 36.14 (對應第二欄)
        sheet.setColumnWidth(2, 25052); // 設定欄寬為 97.86 (對應第三欄)
        sheet.setColumnWidth(3, 3906); // 設定欄寬為 15.29 (對應第四欄)
        sheet.setColumnWidth(4, 3906); // 設定欄寬為 15.29 (對應第五欄)
        sheet.setColumnWidth(5, 15029); // 設定欄寬為 58.71 (對應第六欄)

        // 設定標題行
        Row headerRow = sheet.createRow(0);

        headerRow.createCell(0).setCellValue("最終財產編號");
        headerRow.createCell(1).setCellValue("名稱");
        headerRow.createCell(2).setCellValue("備註(型號)");
        headerRow.createCell(3).setCellValue("放置處");
        headerRow.createCell(4).setCellValue("保管人");
        headerRow.createCell(5).setCellValue("異動記錄");

        // 設定黃色的填充樣式
        CellStyle yellowStyle = workbook.createCellStyle();
        yellowStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        yellowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 設定綠色的填充樣式
        CellStyle greenStyle = workbook.createCellStyle();
        greenStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        greenStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 設定紅色的填充樣式
        CellStyle redStyle = workbook.createCellStyle();
        redStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
        redStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 設定自動換行樣式
        CellStyle wrapTextStyle = workbook.createCellStyle();
        wrapTextStyle.setWrapText(true);  // 設定自動換行
        // 填充資料
        int rowNum = 1;
        for (Inventory_Equipment inventoryequipment : inventoryequipmentList) {
            Row row = sheet.createRow(rowNum++);
            // 設定列高為 16.5 (16.5 * 20 = 330)

            row.setHeight((short) 330);
            // 設定每個單元格自動換行
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(inventoryequipment.getFinal_property_id());
            cell0.setCellStyle(wrapTextStyle);


            Cell cell1 = row.createCell(1);
            cell1.setCellValue(inventoryequipment.getName());
            cell1.setCellStyle(wrapTextStyle);

            Cell cell2 = row.createCell(2);
            cell2.setCellValue(inventoryequipment.getRemarks());
            cell2.setCellStyle(wrapTextStyle);

            Cell cell3 = row.createCell(3);
            cell3.setCellValue(inventoryequipment.getLocation());
            cell3.setCellStyle(wrapTextStyle);

            Cell cell4 = row.createCell(4);
            cell4.setCellValue(inventoryequipment.getCustodian());
            cell4.setCellStyle(wrapTextStyle);

            Cell cell5 = row.createCell(5);
            cell5.setCellValue(inventoryequipment.getChange_record());
            cell5.setCellStyle(wrapTextStyle);
            if (inventoryequipment.getState() != null && inventoryequipment.getState()) {
                //inventory.getState=true 已盤點 設置為綠色
                System.out.println(inventoryequipment.getState());
                System.out.println("test");
                for (int i = 0; i < 6; i++) { // 有 6個欄位
                    row.getCell(i).setCellStyle(greenStyle);
                }
            } else if (inventoryequipment.getIs_updated() != null && inventoryequipment.getIs_updated()) {
                //如果getIs_updated=true 有更新過 設置為黃色
                for (int i = 0; i < 6; i++) {
                    row.getCell(i).setCellStyle(yellowStyle);
                }
            } else {    // 如果 state = false,未盤點 設置紅色背景
                for (int i = 0; i < 6; i++) {
                    row.getCell(i).setCellStyle(redStyle);
                }
            }


        }

        // 將工作簿寫入 ByteArrayOutputStream 中
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        workbook.write(byteArrayOutputStream);
        workbook.close();

        // 設置檔案名稱及頭部信息，並返回檔案下載
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=inventory_report.xlsx");

        return new ResponseEntity<>(byteArrayOutputStream.toByteArray(), headers, HttpStatus.OK);
    }
}
