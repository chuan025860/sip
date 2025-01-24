package org.chyunn_incident.dto;

import lombok.Data;
import org.chyunn_incident.bean.Report;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Data
public class IncidentDto {
    private String incidentId;     // 事件編號
    private String belongingType;  // 歸屬類型
    private String customer;       //事件相關客戶
    private String car;            //事件相關車輛
    private String reporter;       // 反應人
    private String task;           // 工作事項
    private String eventContent;   // 事件內容
    private int reportCount;       // 回報數
    private String importance;     // 重要性
    private String department;     // 處理部門
    private String handler;        // 處理人
    private String status;         // 處理狀態
    private String lastReportTime; // 最後回報時間
    private String reportDate;     // 反應日期
    private Boolean invalid     ;;     //作廢
    private List<Report> reports = new ArrayList<>();

}
