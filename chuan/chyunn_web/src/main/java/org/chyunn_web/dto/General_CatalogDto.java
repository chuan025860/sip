package org.chyunn_web.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class General_CatalogDto {
    private String asset_id; // 財產編號
    private String category; // 類別
    private String name; // 名稱
    private String model; // 型號
    private String serial_or_size ; // 尺寸/序號
    private String quantity_or_unit ; // 數量/單位
    private String purchase_date ; // 購買日期
    private String supplier; // 購入廠商
    private String purchase_price ; // 購買金額
    private String tax; // 稅金
    private String storage_location ; // 放置處
    private String custodian; // 保管人
    private String contact; // 聯繫窗口
    private String change_log ; // 異動記錄
    private Boolean state = false;      // 盤點狀態，預設為 false
    private Boolean is_updated = false; // 檢查是否有更新過，預設為 false
    private String inventory_date;      // 盤點日期
    private List<AssetHistoryDto_All> assetHistoryDtoAlls;
    private List<General_CatalogFile_dto> generalCatalogFileDtos = new ArrayList<>();
}
