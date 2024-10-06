package com.sip.sipproject.repository;

import com.sip.sipproject.bean.Room;
import com.sip.sipproject.bean.RoomQuantityPriceByDate;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface RoomQuantityByDateRepository extends JpaRepository<RoomQuantityPriceByDate, Integer> {

    @Query("SELECT R.quantityByDate FROM RoomQuantityPriceByDate R WHERE R.room = :room AND R.date = :startDate ")
    int getRoomQuantityByDate( @Param(value = "room") Room room, @Param("startDate") Date date);

    @Query("SELECT R FROM RoomQuantityPriceByDate R WHERE R.room = :room AND R.date = :startDate AND R.quantityByDate>=:quantityByDate")
    RoomQuantityPriceByDate getRoomPriceByDate(@Param("room") Room room, @Param("startDate") Date date, @Param("quantityByDate") Integer quantityByDate);

    @Query("from RoomQuantityPriceByDate R where R.room=:room")
    List<RoomQuantityPriceByDate> findRoomQuantityByRoom(@Param(value = "room") Room room);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    //上鎖，ex:可能會導致競爭條件問題。如果兩個用戶同時取消訂單，沒有鎖的話就有可能導致庫存數據不一致、後臺跟使用者 同時更新或取消 也可能導致庫存數據不一致
    @Query("from RoomQuantityPriceByDate R where R.room=:room AND R.date = :startDate")
    RoomQuantityPriceByDate findRoomQuantityByProductID_StartDate_Lock(@Param(value = "room") Room room, @Param("startDate") Date date);

    @Query("from RoomQuantityPriceByDate R where R.room=:room AND R.date = :startDate")
    RoomQuantityPriceByDate findRoomQuantityByProductID_StartDate(@Param(value = "room") Room room, @Param("startDate") Date date);

    @Modifying
    //位置RoomRepository-checkLockRoomQuantity 上鎖-悲觀鎖
    //位置 IndexService 開事務 checkLockRoomQuantity、updateRoomQuantityByDate
    // Transactional交易完成，才會釋放鎖
    @Query("UPDATE RoomQuantityPriceByDate r SET r.quantityByDate = r.quantityByDate - :quantity WHERE r.room = :room AND r.date BETWEEN :startDate AND :endDate")
    void updateRoomQuantityByDate(@Param("quantity") int quantity, @Param("room") Room room, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

}
