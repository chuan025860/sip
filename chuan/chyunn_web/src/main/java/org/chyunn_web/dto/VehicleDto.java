package org.chyunn_web.dto;

import lombok.Data;

@Data
public class VehicleDto {
    private  Integer id;
    private String name;
    private String plate;

    public VehicleDto(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
}
