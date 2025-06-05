package org.chyunn_web.bean.Asset;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "asset_history")
@EqualsAndHashCode(exclude = {"inventoryEquipment"})
public class AssetHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;                  // 每條歷史紀錄的唯一ID

    private String date;              // 記錄日期
    private String custodian;         // 保管人
    private String location;          // 放置處
    private String change_record;      // 異動記錄

    @ManyToOne
    @JoinColumn(name = "inventory_equipment_final_property_id", referencedColumnName = "final_property_id")
    private Inventory_Equipment inventoryEquipment;
}
