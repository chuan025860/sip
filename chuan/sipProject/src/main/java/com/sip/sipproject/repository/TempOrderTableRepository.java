package com.sip.sipproject.repository;

import com.sip.sipproject.bean.TempOrderTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface TempOrderTableRepository  extends JpaRepository<TempOrderTable, String> {

    // 根據創建時間和支付狀態查詢過期且未付款的訂單
    @Query("SELECT t FROM TempOrderTable t WHERE t.orderTime <= :expiryTime")
    List<TempOrderTable> findExpiredOrders(@Param("expiryTime") Date expiryTime);
}
