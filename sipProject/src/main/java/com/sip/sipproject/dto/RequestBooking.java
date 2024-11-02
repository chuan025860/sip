package com.sip.sipproject.dto;

import lombok.Data;

import java.util.List;

@Data
public class RequestBooking {
    private Integer totalPrice;
    private List<RequestRoom> selectedRooms;

}
