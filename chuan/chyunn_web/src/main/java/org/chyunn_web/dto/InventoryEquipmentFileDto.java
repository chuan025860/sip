package org.chyunn_web.dto;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class InventoryEquipmentFileDto {
    private String fileId;
    private String fileName;
    private String final_property_id;
}
