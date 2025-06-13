package org.chyunn_web.dto;

import lombok.Data;

@Data
public class EventDTO {
    private String title;           // 顯示在日曆上的標題（例如 item 或 content）
    private String start;           // ISO 格式時間字串 (yyyy-MM-ddTHH:mm)
    private String end;             // ISO 格式時間字串
    private boolean allDay = false; // 是否為整日事件（預設為 false）

    // 自定義欄位放進 extendedProps
    private String location;
    private String note;
    private String participants;
    private String creator;
    private String creatorId; //借用人
    private String BackgroundColor;
    private String BorderColor;

    private String category;     // 會議室／其他分類
    private String item;         // 會議室名稱

    private String id;

    private String currentUser ;//登入者
    private String currentRole ;//角色

}
