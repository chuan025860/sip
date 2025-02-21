package org.chyunn_incident.bean;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.*;


@Data
@Entity
@Table(name = "incident")
@EqualsAndHashCode(exclude = {"reports","incidentFiles"})
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer incidentId;     // 事件編號
    private String customer;       //事件相關客戶
    private String car;            //事件相關車輛
    private String belongingType;  // 歸屬類型
    private String reporter;       // 反應人
    private String task;           // 工作事項
    private String eventPurpose;   // 事件主旨
    private String eventContent;   // 事件內容
    private int reportCount;       // 回報數
    private String importance;     // 重要性
    private String department;     // 處理部門
    private String handler;        // 處理人
    private String status;         // 處理狀態
    private LocalDateTime lastReportTime; // 最後回報時間
    private Date reportDate;     // 反應日期
    private Boolean invalid= false;     //作廢
    private String creator;      //製表人


    @OneToMany(fetch = FetchType.LAZY, mappedBy = "incident", cascade = CascadeType.ALL)
    private List<Report> reports = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "incident", cascade = CascadeType.ALL)
    private List<IncidentFile> incidentFiles = new ArrayList<>();
}
