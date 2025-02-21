package org.inventory.bean;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "general_catalog_files")
@EqualsAndHashCode(exclude = {"generalCatalog"})
public class GeneralCatalogFile {
    @Id
    @Column(name = "fileId", length = 50, nullable = false)
    private String fileId;

    @Column(name = "filePath", length = 255, nullable = false)
    private String filePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private General_Catalog generalCatalog;
}
