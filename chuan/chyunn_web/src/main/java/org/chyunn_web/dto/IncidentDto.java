package org.chyunn_web.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
public class IncidentDto {
    private Integer incidentId;     // 事件編號
    private String belongingType;  // 歸屬類型
    private String customer;       //事件相關客戶
    private String car;            //事件相關車輛
    private String reporter;       // 反應人
    private String task;           // 工作事項
    private String eventPurpose;   // 事件內容
    private String eventContent;   // 事件內容
    private int reportCount;       // 回報數
    private String importance;     // 重要性
    private String department;     // 處理部門
    private String handler;        // 處理人
    private String status;         // 處理狀態
    private String lastReportTime; // 最後回報時間
    private String reportDate;     // 反應日期
    private Boolean invalid;     //作廢
    private String creator;      //製表人
    private Integer maxIncidentId;     // 最大事件編號(給超連結篩選)
    private Integer totalFiles; //所有檔案數
    private List<IncidentFileDto> incidentFileDtos = new ArrayList<>();
    private List<ReportDto> reportDtos = new ArrayList<>();



}
