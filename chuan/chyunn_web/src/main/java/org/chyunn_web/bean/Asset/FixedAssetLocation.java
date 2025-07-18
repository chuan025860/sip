package org.chyunn_web.bean.Asset;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "fixed_asset_location")
public class FixedAssetLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String name;
}
