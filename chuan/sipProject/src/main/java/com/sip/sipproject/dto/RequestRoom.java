package com.sip.sipproject.dto;

import com.sip.sipproject.bean.*;
import lombok.Data;

@Data
public class RequestRoom {
    private Room room;
    private RoomQuantityPriceByDate roomQuantityPriceByDate;
    private RoomPicture roomPicture;
    private String dateStart;
    private String dateEnd;
    private Integer hotelID;
    private String productID;
    private Integer quantity;
    private String productName;
    private String productType;
    private Integer capacity;
    private Integer orderItemPrice;
    private double discountPercentage;

}
