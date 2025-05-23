package org.chyunn_web.bean.Resource;

import jakarta.persistence.*;
import lombok.Data;
import org.chyunn_web.bean.User.User;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "resource_borrow_request ")
public class ResourceBorrowRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String content;         // 內容
    private String category;        // 類別（如：會議室）
    private String item;            // 項目（會議室名稱）
    private String location;        // 位置
    private String note;            // 備註
    private String participants;    // 參與人員
    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "creator", referencedColumnName = "loginId", nullable = false)
    private User creator;  // 建立者關聯 user.loginId //借用人
}
