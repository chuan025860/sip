package org.chyunn_incident.bean;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.nio.file.Paths;

@Data
@Entity
@Table(name = "incident_files")
@EqualsAndHashCode(exclude = {"incident"})
public class IncidentFile {
    @Id
    private String fileId;
    private String filePath; // 檔案存放路徑

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incidentId")
    private Incident incident;

    // 取得檔案名稱
    public String getFileName() {
        return Paths.get(filePath).getFileName().toString();
    }
}
