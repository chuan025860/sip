package org.chyunn_web.dto;

import jakarta.persistence.Id;
import lombok.Data;

import java.util.List;

@Data
public class FixedAssetDto {
    private String major_category_id;             // 財產大類編號
    private String major_category_name;           // 財產大類名稱
    private String sub_category_id;               // 財產細項編號
    private String sub_category_name;             // 財產細項名稱
    private String fixed_location;                    // 所在地
    private String unit;                        // 單位
    private String acquisition_date;          // 取得時間
    private Integer useful_life_original;         // 耐用年數-原表
    private Integer useful_life_new;              // 耐用年數-新表
    private Integer useful_life_converted;        // 耐用年數-換算
    private String depreciation_end_date;      // 截止提列日期
    private String remark;                      // 備註
    private Integer quantity;                   // 數量
    private String acquisition_cost;         // 取得原價
    private String improvement_cost;         // 改良或修理
    private String salvage_value;            // 預留殘值
    private String net_acquisition_cost;      // 取得原價減預留殘值
    private String current_period_depreciation; // 本期提列數
    private String accumulated_depreciation;   // 截至本期累計數
    private String net_book_value;            // 未折減餘額
    private String improvement_serial_no;         // 改良修理序號
    private String equipment_name;               // 設備或生財器具名稱
    private String catalog_major_name;            // 財產目錄大類名稱
    private String asset_id;                     // ASFA004C
    private String custodian_id;                 // 保管人
    private String custodian_name;               // 保管人名稱
    private String brand_id;                     // 廠牌代號
    private String brand_name;                   // 廠牌名稱
    private String vendor_id;                    // 供應商代號
    private String vendor_name;                  // 廠商名稱
    private String voucher_type;                 // 固資傳票類別
    private String is_depreciation_recalc_excluded; // 不重算註記
    private String is_transferred_to_b_account;    // 轉B帳註記
    private String department_id;                // 部門\工地編號
    private String barcode;                     // 條碼代號
    private String asset_attribute;              // 資產屬性
    private String asset_category;               // 固資分類
    private String depreciation_method;          // 折舊方法
    private String inventory_date;      // 盤點日期
    private Boolean state = false;      // 盤點狀態，預設為 false
    private Boolean inventory_in_progress = false; // 檢查是否有更新過，預設為 false
    private String image_Number;   //圖片數量
    private List<FixedAssetHistoryDto> fixedAssetHistoryDtoList;
    private List<FixedAssetFileDto> fixedAssetFileDtoList;
}
