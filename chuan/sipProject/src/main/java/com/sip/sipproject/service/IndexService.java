package com.sip.sipproject.service;

import com.sip.sipproject.bean.Hotel;
import com.sip.sipproject.bean.Room;
import com.sip.sipproject.repository.RoomQuantityByDateRepository;
import com.sip.sipproject.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Service
public class IndexService {
    @Autowired
    RoomService roomService;
    @Autowired
    RoomRepository roomRepository;
    @Autowired
    RoomQuantityByDateRepository roomQuantityByDateRepository;

    public List<Room> selectIndexRoom(@RequestParam("city") String city,
                                      @RequestParam("checkinDay") LocalDate dateStart,
                                      @RequestParam("checkoutDay") LocalDate dateEnd,
                                      @RequestParam("quantity") Integer quantity,
                                      @RequestParam("totalPeople") Integer totalPeople) {
        //LocalDate 轉換為 Date 類型
        //startDateAsDate :開始日期
        //adjustedEndDateAsDate:調整後的結束日期
        // Date startDateAsDate = java.sql.Date.valueOf(startDate);
        // Date adjustedEndDateAsDate = java.sql.Date.valueOf(endDate);
        Date startDateAsDate = java.sql.Date.valueOf(dateStart);
        Date adjustedEndDateAsDate = java.sql.Date.valueOf(dateEnd);
        // 條件 COUNT(Q.date) = DATEDIFF(day, '2024-09-15', '2024-09-18') + 1     HQL沒有DATEDIFF 改成JAVA實作
        //這個條件的目的是保證該房間在指定的日期範圍內（9月15日到9月18日）每天都有數據記錄。換句話說，如果 DATEDIFF 計算出的天數範圍是 4 天，COUNT(Q.date) 也必須是 4，這樣才能說明房間的日期數據是完整的。
        //如果某一天的 Q.date 是空的，那麼 COUNT(Q.date) 的結果就會小於 4，這個房間就會被過濾掉。
        long dateDifference = ChronoUnit.DAYS.between(dateStart, dateEnd) + 1;
        List<Room> rooms = roomRepository.selectIndexRoom(city, startDateAsDate, adjustedEndDateAsDate, quantity, totalPeople, dateDifference);

        return rooms;
    }

    public List<Room> selectOtherRooms(@Param(value = "hotel") Hotel hotel,
                                       @RequestParam("checkinDay") LocalDate dateStart,
                                       @RequestParam("checkoutDay") LocalDate dateEnd) {

        Date startDateAsDate = java.sql.Date.valueOf(dateStart);
        Date adjustedEndDateAsDate = java.sql.Date.valueOf(dateEnd);
        long dateDifference = ChronoUnit.DAYS.between(dateStart, dateEnd) + 1;
        List<Room> selectOtherRooms = roomRepository.selectOtherRooms(hotel, startDateAsDate, adjustedEndDateAsDate, dateDifference);
        return selectOtherRooms;
    }


    public Room checkRoomQuantity(@Param(value = "productID") String productID,
                                  @RequestParam("quantity") Integer quantity,
                                  @RequestParam("checkinDay") Date dateStart,
                                  @RequestParam("checkoutDay") Date dateEnd) {
        Room checkRoomQuantity = roomRepository.checkRoomQuantity(productID, quantity, dateStart, dateEnd);

        return checkRoomQuantity;
    }


    public Boolean checkAndUpdateRoomQuantity(@Param(value = "productID") String productID,
                                           @RequestParam("quantity") Integer quantity,
                                           @RequestParam("checkinDay") Date dateStart,
                                           @RequestParam("checkoutDay") Date dateEnd) {
        Room room = roomRepository.checkLockRoomQuantity(productID, quantity, dateStart, dateEnd);
        if (room != null) {
            roomQuantityByDateRepository.updateRoomQuantityByDate(quantity, roomService.findByProductID(productID), dateStart, dateEnd);
            return true;
        }else {
            return false;
        }
    }

    public Boolean checkAndRestoreRoomQuantityByDate(@Param(value = "productID") String productID,
                                              @RequestParam("quantity") Integer quantity,
                                              @RequestParam("checkinDay") Date dateStart,
                                              @RequestParam("checkoutDay") Date dateEnd) {
        Room room = roomRepository.checkLockRoomQuantity(productID, quantity, dateStart, dateEnd);
        if (room != null) {
            roomQuantityByDateRepository.restoreRoomQuantityByDate(quantity, roomService.findByProductID(productID), dateStart, dateEnd);
            return true;
        }else {
            return false;
        }
    }


    public Integer UndiscountedTotalpPrice(@Param(value = "productID") String productID,
                                           @RequestParam("quantity") Integer quantity,
                                           @RequestParam("checkinDay") Date dateStart,
                                           @RequestParam("checkoutDay") Date dateEnd) {
        Integer UndiscountedTotalpPrice = roomRepository.UndiscountedTotalpPrice(productID, quantity, dateStart, dateEnd);

        return UndiscountedTotalpPrice;
    }

    public Integer totalRoomPrice(@Param(value = "productID") String productID,
                                  @RequestParam("quantity") Integer quantity,
                                  @RequestParam("checkinDay") Date dateStart,
                                  @RequestParam("checkoutDay") Date dateEnd) {
        Integer totalRoomPrice = roomRepository.totalRoomPrice(productID, quantity, dateStart, dateEnd);

        return totalRoomPrice;
    }

    public Integer findMinimumQuantityByDate(@Param(value = "productID") String productID,
                                             @RequestParam("checkinDay") Date dateStart,
                                             @RequestParam("checkoutDay") Date dateEnd) {
        Integer minQuantity = roomRepository.findMinimumQuantityByDate(productID, dateStart, dateEnd);

        return minQuantity;
    }
}
