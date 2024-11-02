package com.sip.sipproject.dto;

import com.sip.sipproject.bean.Hotel;
import com.sip.sipproject.bean.HotelDetail;
import lombok.Data;

@Data
public class HotelRequest {
    private Integer hotelID;
    private Hotel hotel;
    private HotelDetail hotelDetail;
}
