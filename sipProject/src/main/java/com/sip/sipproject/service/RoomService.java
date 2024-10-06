package com.sip.sipproject.service;

import com.sip.sipproject.bean.*;
import com.sip.sipproject.repository.HotelRepository;
import com.sip.sipproject.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private HotelRepository hotelRepository;

    public Room findByProductID(String productID) {
        Optional<Room> optional = roomRepository.findById(productID);
        if (optional.isPresent()) {
            return optional.get();
        } else {
            return null;
        }
    }
    public void insterRoom(Hotel hotel,Room room, RoomQuantityPriceByDate roomQuantityPriceByDate,String dateStart,String dateEnd,List<MultipartFile> productImages) throws IOException {
        IdGenerator forRoom = new IdGenerator();
        room.setProductID(forRoom.generateId());
        if (!room.getChildExtraBed()){
            room.setAge(null);
            room.setChildrenPrice(null);
        }
        room.setHotel(hotel);// 建立多對一雙向關聯(房間-使用者)

        List<RoomQuantityPriceByDate> roomQuantityPriceByDates = new ArrayList<>();
        // 使用日期範圍創建對象
        LocalDate StartDate = LocalDate.parse(dateStart);
        LocalDate endDate =LocalDate.parse(dateEnd);
        while (!StartDate.isAfter(endDate)) {
            //check日期
            System.out.println(StartDate);
            // 為每天建立一個新的 RoomQuantityPriceByDate
            RoomQuantityPriceByDate newRoomQuantityPriceByDate = new RoomQuantityPriceByDate();
            newRoomQuantityPriceByDate.setQuantityByDate(roomQuantityPriceByDate.getQuantityByDate());
            newRoomQuantityPriceByDate.setDate(java.sql.Date.valueOf(StartDate));
            newRoomQuantityPriceByDate.setPrice(roomQuantityPriceByDate.getPrice());
            newRoomQuantityPriceByDate.setRoom(room); // 建立多對一雙向關聯
            roomQuantityPriceByDates.add(newRoomQuantityPriceByDate);
            StartDate = StartDate.plusDays(1);
        }
        room.setRoomQuantityByDates(roomQuantityPriceByDates);// 建立一對多雙向關聯

        List<RoomPicture> roomPictures = new ArrayList<>();
        for (MultipartFile productImage : productImages) {
            RoomPicture roomPicture = new RoomPicture();
            roomPicture.setImageData(productImage.getBytes());
            roomPicture.setRoom(room);// 建立多對一雙向關聯
            roomPictures.add(roomPicture);
        }
        room.setRoomPictures(roomPictures);// 建立一對多雙向關聯

        roomRepository.save(room);
    }
    public void updateRoom(Hotel hotel,Room room) throws IOException {
        if (!room.getChildExtraBed()){
            room.setAge(null);
            room.setChildrenPrice(null);
        }
        room.setHotel(hotel);// 建立多對一雙向關聯
        roomRepository.save(room);
    }

    public void updateRoomState(Room room) {
        if (room.getState()) {
            room.setState(false);
            roomRepository.save(room);
        } else {
            room.setState(true);
            roomRepository.save(room);
        }
    }

    public List<Room> findRoomsByhotel(Hotel hotel) {
            List<Room> rooms = roomRepository.findAllRoomByhotel(hotel);
            return rooms;
    }

    //////////////////10/25更新//////////////////////////////////
    public List<Room> findRoom(Hotel hotel, Date startDate, Date endDate,Long dateDifference) {
        List<Room> rooms = roomRepository.findRoom(hotel,startDate,endDate,dateDifference);
        return rooms;
    }

}
