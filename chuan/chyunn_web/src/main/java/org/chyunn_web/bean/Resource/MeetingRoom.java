package org.chyunn_web.bean.Resource;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name="meeting_room")
public class MeetingRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String location;
}