package org.chyunn_web.bean;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "randomsampling_all ")
public class RandomSampling_All {
    @Id
    private String asset_id;   // 最終財產編號
    private Boolean state = false;      // 盤點狀態，預設為 false
    private Boolean inventory_in_progress  = false;      // 表示是否在進行盤點中，預設為 false
}
