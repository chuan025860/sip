package com.sip.sipproject.dto;

import com.sip.sipproject.bean.Room;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class RoomDTO {
    private String productID;
    private String productName;
    private String productType;
    private Integer price;
    private Integer minQuantity;
    private Integer capacity;
    private Date dateStart;
    private Date dateEnd;
    private double discountPercentage;

    public static List<RoomDTO> findRoom(List<Room> rooms) {
        List<RoomDTO>  roomDTOS=new ArrayList<>();
        for (Room room : rooms) {
            RoomDTO roomDTO = new RoomDTO();
            roomDTO.setProductID(room.getProductID());
            roomDTO.setProductName(room.getProductName());
            roomDTO.setProductType(room.getProductType());
            roomDTO.setMinQuantity(room.getMinQuantity());
            roomDTO.setPrice(room.getPrice());
            roomDTO.setCapacity(room.getCapacity());
            roomDTO.setDiscountPercentage(room.getDiscountPercentage());
            roomDTOS.add(roomDTO);
        }
        return roomDTOS;
    }
}
