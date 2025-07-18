package org.chyunn_web.dto;

import jakarta.persistence.*;
import lombok.Data;
import org.chyunn_web.bean.User.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Data

public class BulletinPostDto {

    private Integer id;                  // 自動編號主鍵
    private String title;               // 文章標題
    private String content;             // 文章內容（含 CKEditor HTML）
    private LocalDateTime publishTime;  // 發佈時間
    private LocalDateTime endTime;  // 發佈時間
    private LocalDateTime created_at;    // 建立時間
    private LocalDateTime updated_at;    // 更新時間
    private String publishTimeStr;
    private String endTimeStr;
    private String creatorName;
    private List<BulletinFileDto> bulletinFileDtos = new ArrayList<>();

}
