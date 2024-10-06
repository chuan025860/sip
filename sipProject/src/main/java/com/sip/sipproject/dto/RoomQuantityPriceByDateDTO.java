package com.sip.sipproject.dto;

import lombok.Data;

import java.util.Date;

@Data
public class RoomQuantityPriceByDateDTO {
    private String  roomQuantityID;
    private Integer quantityByDate;
    private Integer price;
    private Date date;
    private Integer discountPrice;
}
