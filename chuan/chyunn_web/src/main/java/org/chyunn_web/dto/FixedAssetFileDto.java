package org.chyunn_web.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.Data;

@Data
public class FixedAssetFileDto {
    private String fileId;
    private String fileName;
    private String sub_category_id;
}
