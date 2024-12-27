package com.sip.sipproject.repository;


import com.sip.sipproject.bean.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, String> {


}
