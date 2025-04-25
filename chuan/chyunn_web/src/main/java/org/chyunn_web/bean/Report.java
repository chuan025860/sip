package org.chyunn_web.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "report")
@EqualsAndHashCode(exclude = {"incident","reportFiles"})
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reportId;
    private LocalDateTime reportTime; // 回報時間
    private String reporter;         // 回報人
    private String progress;         // 處理進度
    private Date executionDate; // 執行日期
    private String executor;         // 執行人
    private String content;          // 內容

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incidentId")
    private Incident incident;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "report", cascade = CascadeType.ALL)
    private List<ReportFile> reportFiles = new ArrayList<>();

    @Transient
    private Integer incidentId;
    @Transient
    private String reportTimeString; // 回報時間
    @Transient
    private String executionDateString; // 執行日期
}
