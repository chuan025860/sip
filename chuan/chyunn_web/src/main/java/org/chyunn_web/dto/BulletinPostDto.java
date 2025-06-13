package org.chyunn_web.dto;

import jakarta.persistence.*;
import lombok.Data;
import org.chyunn_web.bean.User.User;

import java.time.LocalDateTime;


@Data

public class BulletinPostDto {

    private Integer id;                  // 自動編號主鍵
    private String title;               // 文章標題
    private String content;             // 文章內容（含 CKEditor HTML）
    private LocalDateTime publish_time;  // 發佈時間
    private LocalDateTime created_at;    // 建立時間
    private LocalDateTime updated_at;    // 更新時間
    private String publishTimeStr;
    private String creatorName;

}
