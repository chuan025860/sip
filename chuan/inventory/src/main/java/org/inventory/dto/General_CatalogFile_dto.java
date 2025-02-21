package org.inventory.dto;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class General_CatalogFile_dto {
    private String fileId;

    private String filePath;
}
