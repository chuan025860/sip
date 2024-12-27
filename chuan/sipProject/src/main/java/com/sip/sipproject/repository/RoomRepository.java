package com.sip.sipproject.repository;

import java.util.Date;
import java.util.List;

import com.sip.sipproject.bean.Hotel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sip.sipproject.bean.Room;


public interface RoomRepository extends JpaRepository<Room, String> {

    @Query(value = "from Room r WHERE r.hotel= :hotel ORDER BY r.createDate ASC")
    List<Room> findAllRoomByhotel(@Param(value = "hotel") Hotel hotel);

    @Query("SELECT R " +
            "FROM Room R " +
            "INNER JOIN R.hotel H " +
            "INNER JOIN R.roomQuantityByDates Q " +  // 連接 Room 和 RoomQuantityPriceByDate
            "WHERE R.capacity >= :totalPeople / :quantity " +
            "AND LOWER(H.city) LIKE LOWER(CONCAT('%', :city, '%')) " +
            "AND H.state = true " +            // 判斷飯店是否營業
            "AND R.state = true " +            // 判斷房間是否可用
            "AND Q.date BETWEEN :checkInDate AND :checkOutDate " +  // 過濾日期範圍
            "GROUP BY R " +                    // 按房間分組
            //  CASE WHEN Q.quantityByDate > 2 THEN 0 ELSE 1 END：
            //當 Q.quantityByDate >= quantity 時，返回 0。這表示房間在某一天的數量大於 搜尋數量。
            //當 Q.quantityByDate < quantity 時，返回 1。這表示房間在某一天的數量小於或等於 搜尋數量。
            //SUM(CASE ...) = 0：
            //SUM 函數會將 CASE 表達式的結果加總。如果所有的 Q.quantityByDate 都大於 quantity，CASE 表達式會每次返回 0，所以 SUM 的結果為 0。
            //當 SUM(CASE ...) = 0 時，表示所有日期範圍內，房間的數量 每一天都大於 quantity。
            //結果解釋
            //這個 HAVING 條件篩選出那些在所有指定日期範圍內，房間的數量 每一天都大於 2 的房間。
            //如果 SUM(CASE ...) 結果不為 0，則意味著至少有一天的房間數量是小於或等於 2，這樣的房間會被排除。
            "HAVING SUM(CASE WHEN Q.quantityByDate >= :quantity THEN 0 ELSE 1 END) = 0" +
            //  INNER JOIN 並不會過濾掉日期缺失的情況。因此，SUM(CASE WHEN Q.quantityByDate > quantity THEN 0 ELSE 1 END) 還是會返回 0，即使某一天的數據是 null。
            //處理問題，通過排除 null 值，確保所有日期範圍內都有相應的記錄，使用 COUNT 來檢查日期數據的完整性。
            "AND COUNT(Q.date) = :dateDifference"
    )
        // 確保所有日期的房間數量都符合要求
    List<Room> selectIndexRoom(@Param("city") String city,
                               @Param("checkInDate") Date checkInDate,
                               @Param("checkOutDate") Date checkOutDate,
                               @Param("quantity") Integer quantity,
                               @Param("totalPeople") Integer totalPeople,
                               @Param("dateDifference") Long dateDifference);

    @Query("SELECT R " +
            "FROM Room R " +
            "INNER JOIN R.roomQuantityByDates Q " +
            "WHERE R.hotel= :hotel " +
            "AND Q.date BETWEEN :checkInDate AND :checkOutDate " +   // 過濾日期範圍
            "GROUP BY R " +                    // 按房間分組
            "HAVING SUM(CASE WHEN Q.quantityByDate >= 1 THEN 0 ELSE 1 END) = 0" +
            "AND COUNT(Q.date) = :dateDifference"
    )
    List<Room> selectOtherRooms(@Param(value = "hotel") Hotel hotel,
                                @Param("checkInDate") Date checkInDate,
                                @Param("checkOutDate") Date checkOutDate,
                                @Param("dateDifference") Long dateDifference);

    @Query("SELECT R " +
            "FROM Room R " +
            "INNER JOIN R.roomQuantityByDates Q " +
            "WHERE R.productID= :productID " +
            "AND Q.date BETWEEN :checkInDate AND :checkOutDate " +
            "GROUP BY R " +                    // 按房間分組
            "HAVING SUM(CASE WHEN Q.quantityByDate >= :quantity THEN 0 ELSE 1 END) = 0"   // 過濾日期範圍
    )
    Room checkRoomQuantity(@Param(value = "productID") String productID,
                           @Param("quantity") Integer quantity,
                           @Param("checkInDate") Date checkInDate,
                           @Param("checkOutDate") Date checkOutDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    //交易完成，才會釋放鎖
    @Query("SELECT R " +
            "FROM Room R " +
            "INNER JOIN R.roomQuantityByDates Q " +
            "WHERE R.productID= :productID " +
            "AND Q.date BETWEEN :checkInDate AND :checkOutDate " +
            "GROUP BY R " +                    // 按房間分組
            "HAVING SUM(CASE WHEN Q.quantityByDate >= :quantity THEN 0 ELSE 1 END) = 0"   // 過濾日期範圍
    )
    Room checkLockRoomQuantity(@Param(value = "productID") String productID,
                               @Param("quantity") Integer quantity,
                               @Param("checkInDate") Date checkInDate,
                               @Param("checkOutDate") Date checkOutDate);

    @Query("SELECT SUM(COALESCE(Q.discountPrice, Q.price)) " +
            "FROM Room R " +
            "INNER JOIN R.roomQuantityByDates Q " +
            "WHERE R.productID= :productID " +
            "AND Q.quantityByDate >= :quantity " +
            "AND Q.date BETWEEN :checkInDate AND :checkOutDate "   // 過濾日期範圍
    )
    Integer totalRoomPrice(@Param(value = "productID") String productID,
                           @Param("quantity") Integer quantity,
                           @Param("checkInDate") Date checkInDate,
                           @Param("checkOutDate") Date checkOutDate);


    @Query("SELECT SUM(Q.price) " +
            "FROM Room R " +
            "INNER JOIN R.roomQuantityByDates Q " +
            "WHERE R.productID= :productID " +
            "AND Q.quantityByDate >= :quantity " +
            "AND Q.date BETWEEN :checkInDate AND :checkOutDate "   // 過濾日期範圍
    )
    Integer UndiscountedTotalpPrice(@Param(value = "productID") String productID,
                                    @Param("quantity") Integer quantity,
                                    @Param("checkInDate") Date checkInDate,
                                    @Param("checkOutDate") Date checkOutDate);


    @Query("SELECT MIN(Q.quantityByDate) " +
            "FROM Room R " +
            "INNER JOIN R.roomQuantityByDates Q " +
            "WHERE R.productID = :productID " +
            "AND Q.date BETWEEN :checkInDate AND :checkOutDate "   // 過濾日期範圍
    )
    Integer findMinimumQuantityByDate(@Param("productID") String productID,
                                      @Param("checkInDate") Date checkInDate,
                                      @Param("checkOutDate") Date checkOutDate);


    @Query("SELECT R " +
            "FROM Room R " +
            "INNER JOIN R.roomQuantityByDates Q " +
            "WHERE R.hotel= :hotel " +
            "AND Q.date BETWEEN :checkInDate AND :checkOutDate " +   // 過濾日期範圍
            "GROUP BY R " +                    // 按房間分組
            "HAVING SUM(CASE WHEN Q.quantityByDate >= 1 THEN 0 ELSE 1 END) = 0" +
            "AND COUNT(Q.date) = :dateDifference"
    )
        // 確保所有日期的房間數量都符合要求
    List<Room> findRoom(@Param(value = "hotel") Hotel hotel,
                        @Param("checkInDate") Date checkInDate,
                        @Param("checkOutDate") Date checkOutDate,
                        @Param("dateDifference") Long dateDifference);
}
