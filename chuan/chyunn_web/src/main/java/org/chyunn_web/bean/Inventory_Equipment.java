package org.chyunn_web.bean;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "inventory_equipment")
@EqualsAndHashCode(exclude = {"inventoryEquipmentFiles"})
public class Inventory_Equipment {
    private String property_id;        // 財產編號
    private String management_id;      // 列管編號
    @Id
    private String final_property_id;   // 最終財產編號
    private String category;          // 類別
    private String name;              // 名稱
    private String remarks;           // 備註
    private String size_serial;        // 尺寸_序號
    private String quantity_unit;      // 數量_單位
    private String purchase_date;      // 購買日期
    private String supplier;          // 購入廠商
    private String purchase_amount; // 購買金額
    private String tax;           // 稅金
    private String location;          // 放置處
    private String custodian;         // 保管人
    private String contact_window;     // 聯繫窗口
    private String change_record;      // 異動記錄
    private Boolean state = false;      // 盤點狀態，預設為 false
    private Boolean inventory_in_progress  = false;      // 表示是否在進行盤點中，預設為 false
    private String inventory_date;      // 盤點日期
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "inventoryEquipment", cascade = CascadeType.ALL)
    private List<InventoryEquipmentFile> inventoryEquipmentFiles = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "inventoryEquipment", cascade = CascadeType.ALL)
    private List<AssetHistory> assetHistoryList;  // 該設備的所有歷史紀錄
}
