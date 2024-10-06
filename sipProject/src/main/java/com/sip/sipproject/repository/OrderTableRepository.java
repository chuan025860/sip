package com.sip.sipproject.repository;

import com.sip.sipproject.bean.Customer;
import com.sip.sipproject.bean.Hotel;
import com.sip.sipproject.bean.OrderTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface OrderTableRepository extends JpaRepository<OrderTable, String> {
    @Query("SELECT o FROM OrderTable o WHERE   o.orderID = :orderID AND  o.hotel = :hotel")
    OrderTable findOrderByOrderID_hotel(@Param("orderID") String orderID,@Param(value = "hotel") Hotel hotel);
    @Query("SELECT o FROM OrderTable o WHERE   o.orderID = :orderID AND  o.customer = :customer")
    OrderTable findOrderByOrderID_customer(@Param("orderID") String orderID,@Param(value = "customer") Customer customer);

    @Query("SELECT o FROM OrderTable o WHERE o.hotel = :hotel AND o.checkInDate BETWEEN :startDate AND :endDate ORDER BY o.checkInDate ASC")
    List<OrderTable> findOrdersByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("hotel") Hotel hotel);

    @Query("SELECT o FROM OrderTable o WHERE o.hotel = :hotel " +
            "AND ((:status = '進行中' AND o.checkInDate <= :currentDate AND o.checkOutDate >= :currentDate AND o.orderStatus != 'canceled') " +
            "OR (:status = '有效' AND (o.checkInDate > :currentDate OR (o.checkInDate <= :currentDate AND o.checkOutDate >= :currentDate))AND o.orderStatus != 'canceled') " +
            "OR (:status = '過期' AND o.checkOutDate < :currentDate AND o.orderStatus != 'canceled') " +
            "OR o.orderStatus = :status) " +
            "ORDER BY o.checkInDate ASC")
    List<OrderTable> findOrderByHotelAndStatus(@Param("status") String status,
                                               @Param("hotel") Hotel hotel,
                                               @Param("currentDate") Date currentDate);

    @Query("SELECT o FROM OrderTable o WHERE o.checkInDate >= :comingDay AND  o.hotel = :hotel AND o.orderStatus != 'canceled' ORDER BY o.checkInDate ASC")
    List<OrderTable> upcomingOrders(@Param("comingDay") Date comingDay,@Param(value = "hotel") Hotel hotel);

    @Query("SELECT o FROM OrderTable o WHERE o.customer = :customer " +
            "AND ((:status = '進行中' AND o.checkInDate <= :currentDate AND o.checkOutDate >= :currentDate AND o.orderStatus != 'canceled') " +
            "OR (:status = '有效' AND (o.checkInDate > :currentDate OR (o.checkInDate <= :currentDate AND o.checkOutDate >= :currentDate))AND o.orderStatus != 'canceled') " +
            "OR (:status = '過期' AND o.checkOutDate < :currentDate AND o.orderStatus != 'canceled') " +
            "OR o.orderStatus = :status) " +
            "ORDER BY o.checkInDate ASC")
    List<OrderTable> findOrderByCustomerAndStatus(@Param("status") String status,
                                               @Param("customer") Customer customer,
                                               @Param("currentDate") Date currentDate);


    @Query("SELECT o FROM OrderTable o WHERE o.checkInDate >= :comingDay AND  o.customer = :customer AND o.orderStatus != 'canceled' ORDER BY o.checkInDate ASC")
    List<OrderTable> upcomingOrdersByCustomer(@Param("comingDay") Date comingDay,@Param(value = "customer") Customer customer);

    @Query("SELECT o FROM OrderTable o WHERE o.customer = :customer AND o.checkOutDate BETWEEN :startDate AND :endDate ORDER BY o.checkInDate ASC")
    List<OrderTable> findExpiredOrdersByDateRangeByCustomer(@Param("startDate") Date startDate, @Param("endDate") Date endDate,@Param(value = "customer") Customer customer);
}
