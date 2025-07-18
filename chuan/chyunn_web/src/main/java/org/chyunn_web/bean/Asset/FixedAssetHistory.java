package org.chyunn_web.bean.Asset;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "fixed_asset_history")
@EqualsAndHashCode(exclude = {"fixedAsset"})
public class FixedAssetHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;                  // 每條歷史紀錄的唯一ID

    private String date;              // 記錄日期
    private String custodian;         // 保管人
    private String fixed_location;          // 放置處
    private String change_record;      // 異動記錄

    @ManyToOne
    @JoinColumn(name = "sub_category_id", referencedColumnName = "sub_category_id")
    private FixedAsset fixedAsset;
}
