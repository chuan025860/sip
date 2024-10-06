package com.sip.sipproject.service;

import com.sip.sipproject.bean.Room;
import com.sip.sipproject.bean.RoomQuantityPriceByDate;
import com.sip.sipproject.repository.RoomQuantityByDateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class RoomQuantityByDateService {
    @Autowired
    RoomQuantityByDateRepository roomQuantityByDateRepository;

    public List<RoomQuantityPriceByDate> findRoomQuantityByRoom(Room room) {
        return roomQuantityByDateRepository.findRoomQuantityByRoom(room);
    }

    public void updateRoomQuantity(Room room, String dateStart, String dateEnd, String price, String quantity, String discountOptions, String discountPrice) {
        LocalDate localDateStart = LocalDate.parse(dateStart);
        LocalDate localDateEnd = LocalDate.parse(dateEnd);
        List<RoomQuantityPriceByDate> roomQuantityList = new ArrayList<>();
        while (!localDateStart.isAfter(localDateEnd)) {
            RoomQuantityPriceByDate roomQuantityPriceByDate = roomQuantityByDateRepository.findRoomQuantityByProductID_StartDate(room, java.sql.Date.valueOf(localDateStart));
            if (roomQuantityPriceByDate == null) {
                roomQuantityPriceByDate = new RoomQuantityPriceByDate();
                roomQuantityPriceByDate.setRoom(room);
                roomQuantityPriceByDate.setDate(java.sql.Date.valueOf(localDateStart));
            }
            roomQuantityPriceByDate.setPrice(Integer.parseInt(price));
            roomQuantityPriceByDate.setQuantityByDate(Integer.parseInt(quantity));
            // 判斷是否有折扣
            if ("noDiscount".equals(discountOptions) || "null".equals(discountPrice)) {
                roomQuantityPriceByDate.setDiscountPrice(null);
            } else if // 判斷是否有自訂折扣 或 有折扣值
            ("customize".equals(discountOptions) || discountPrice != null) {
                roomQuantityPriceByDate.setDiscountPrice(Integer.parseInt(discountPrice));
            }
            // 逐個更新
            //roomQuantityByDateRepository.save(roomQuantityPriceByDate);
            roomQuantityList.add(roomQuantityPriceByDate);
            localDateStart = localDateStart.plusDays(1);
        }
        //批量更新
        roomQuantityByDateRepository.saveAll(roomQuantityList);
    }

    public Boolean restoreRoomQuantity(Room room, Integer quantity, Date checkInDate, Date checkOutDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateStart = dateFormat.format(checkInDate);
        String dateEnd = dateFormat.format(checkOutDate);
        LocalDate localDateStart = LocalDate.parse(dateStart);
        LocalDate localDateEnd = LocalDate.parse(dateEnd).minusDays(1);
        while (!localDateStart.isAfter(localDateEnd)) {
            RoomQuantityPriceByDate roomQuantityPriceByDate = roomQuantityByDateRepository.findRoomQuantityByProductID_StartDate_Lock(room, java.sql.Date.valueOf(localDateStart));

            // 檢查 roomQuantityPriceByDate 是否為 null
            if (roomQuantityPriceByDate == null) {
                // 處理找不到對應的房間數量情況
                return false;
            }
            // 檢查更新後的數量是否有效
            if (roomQuantityPriceByDate.getQuantityByDate() + quantity < 0) {
                // 處理數量不足的情況
                return false;
            }
            roomQuantityPriceByDate.setQuantityByDate(roomQuantityPriceByDate.getQuantityByDate() + quantity);
            localDateStart = localDateStart.plusDays(1);
        }
        return true;
    }
}
