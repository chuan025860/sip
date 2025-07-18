package org.chyunn_web.dto;

import lombok.Data;

@Data
public class MeetingRoomDto {
    private  Integer id;
    private String name;
    private String location;

    public MeetingRoomDto(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
}
