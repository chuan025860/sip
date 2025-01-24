package org.chyunn_incident.dto;

import jakarta.persistence.*;
import lombok.Data;
import org.chyunn_incident.bean.Incident;

import java.time.LocalDateTime;
import java.util.Date;
@Data
public class ReportDto {

    private Integer reportId;
    private LocalDateTime reportTime; // 回報時間
    private String reporter;         // 回報人
    private String progress;         // 處理進度
    private Date executionDate; // 執行日期
    private String executor;         // 執行人
    private String content;          // 內容


    private String reportTimeString; // 回報時間
    private String executionDateString; // 執行日期
}
