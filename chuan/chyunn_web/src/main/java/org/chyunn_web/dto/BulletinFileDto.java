package org.chyunn_web.dto;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.chyunn_web.bean.Resource.BulletinPost;

import java.nio.file.Paths;

@Data
public class BulletinFileDto {

    private String fileId;

    private String fileName;

    private String filePath; // 檔案存放路徑

    private Integer bulletinId;

    // 取得檔案名稱
    public String getFileName() {
        return Paths.get(filePath).getFileName().toString();
    }
}
