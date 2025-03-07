package org.chyunn_incident.dto;

import lombok.Data;

import java.nio.file.Paths;

@Data
public class IncidentFileDto {
    private String fileId;
    private String filePath; // 檔案存放路徑
    private Integer incidentId;
    // 取得檔案名稱
    public String getFileName() {
        return Paths.get(filePath).getFileName().toString();
    }
}
