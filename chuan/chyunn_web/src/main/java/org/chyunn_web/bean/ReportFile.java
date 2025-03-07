package org.chyunn_web.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.nio.file.Paths;

@Data
@Entity
@Table(name = "report_files")
@EqualsAndHashCode(exclude = {"report"})
public class ReportFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String fileId;
    private String fileName;
    private String filePath; // 檔案存放路徑

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reportId")
    private Report report;

    // 取得檔案名稱
    public String getFileName() {
        return Paths.get(filePath).getFileName().toString();
    }
}
