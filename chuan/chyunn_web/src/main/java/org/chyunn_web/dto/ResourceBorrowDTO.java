package org.chyunn_web.dto;

import lombok.Data;

@Data
public class ResourceBorrowDTO {
    private Integer id;
    private String content;         // 內容
    private String startTime; // 開始時間
    private String endTime;   // 結束時間
    private String category;        // 類別（如：會議室）
    private String item;            // 項目（會議室名稱）
    private String location;        // 位置
    private String note;            // 備註
    private String participants;    // 參與人員
    private String createdAt;// 建立時間
    private String creator; //借用人
    private long daysOld;
}
