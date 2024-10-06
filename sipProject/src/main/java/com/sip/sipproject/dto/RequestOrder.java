package com.sip.sipproject.dto;

import lombok.Data;

@Data
public class RequestOrder {
    private Integer hotelID;
    private String orderID;
    private String selectedDate;
    private String status;
}
