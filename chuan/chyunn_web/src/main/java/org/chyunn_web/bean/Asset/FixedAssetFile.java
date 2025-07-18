package org.chyunn_web.bean.Asset;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "fixed_asset_files")
@EqualsAndHashCode(exclude = {"fixedAsset"})
public class FixedAssetFile {
    @Id
    @Column(name = "fileId", length = 50, nullable = false)
    private String fileId;

    @Column(name = "fileName", length = 255, nullable = false)
    private String fileName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_category_id")
    private FixedAsset fixedAsset;
}
