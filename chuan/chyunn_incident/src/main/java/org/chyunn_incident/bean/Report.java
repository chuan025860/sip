package org.chyunn_incident.bean;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Entity
@Table(name = "report")
@EqualsAndHashCode(exclude = {"incident"})
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incidentId")
    private Incident incident;
    private LocalDateTime reportTime; // 回報時間
    private String reporter;         // 回報人
    private String progress;         // 處理進度
    private Date executionDate; // 執行日期
    private String executor;         // 執行人
    private String content;          // 內容

    @Transient
    private String incidentId;
    @Transient
    private String reportTimeString; // 回報時間
    @Transient
    private String executionDateString; // 執行日期
}
