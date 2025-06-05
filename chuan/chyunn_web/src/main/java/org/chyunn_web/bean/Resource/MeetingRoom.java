package org.chyunn_web.bean.Resource;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name="meeting_room")
public class MeetingRoom {
    @Id
    @GeneratedValue
    Integer id;
    private String name;
    private String location;
}