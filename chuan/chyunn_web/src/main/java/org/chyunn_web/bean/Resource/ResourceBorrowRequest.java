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

    @Column(nullable = false, length = 255)
    private String content;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('ROOM','VEHICLE')")
    private Category category;

    // 對應 meeting_room_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_room_id", referencedColumnName = "id")
    private MeetingRoom meetingRoom;

    // 對應 vehicle_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", referencedColumnName = "id")
    private Vehicle vehicle;

    @Column(length = 255)
    private String location;

    @Column(columnDefinition = "TEXT")
    private String note;

    // 建立者：對應 user.loginId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator", referencedColumnName = "loginId", nullable = false)
    private User creator;

    @Column(columnDefinition = "TEXT")
    private String participants;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "all_day", nullable = false)
    private Boolean allDay = false;

    public enum Category {
        ROOM, VEHICLE,OTHER
    }
    private String item_other;
}
