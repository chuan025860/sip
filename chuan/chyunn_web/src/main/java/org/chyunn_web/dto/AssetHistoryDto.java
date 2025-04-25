package org.chyunn_web.dto;

import lombok.Data;

@Data
public class AssetHistoryDto {
    private Integer id;                  // 每條歷史紀錄的唯一ID
    private String date;                // 記錄日期
    private String custodian;           // 保管人
    private String location;            // 放置處
    private String change_record;      // 異動記錄
    private String inventory_equipment_final_property_id; //資訊財產ID
}
