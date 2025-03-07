package org.chyunn_web.dto;

import lombok.Data;

import java.nio.file.Paths;

@Data
public class ReportFileDto {
    private String fileId;
    private String filePath; // 檔案存放路徑
    // 取得檔案名稱
    public String getFileName() {
        return Paths.get(filePath).getFileName().toString();
    }
}
