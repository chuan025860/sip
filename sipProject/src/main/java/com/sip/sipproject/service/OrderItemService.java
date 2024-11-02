package com.sip.sipproject.service;

import com.sip.sipproject.bean.OrderItem;
import com.sip.sipproject.repository.CustomerRepository;
import com.sip.sipproject.repository.OrderItemRepository;
import com.sip.sipproject.repository.OrderTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderItemService {
    @Autowired
    OrderTableRepository orderTableRepository;
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    OrderItemRepository orderItemRepository;
    public List<OrderItem> findOrderItemByOrderID(String orderID){
    return null;
    }
    public Boolean hasOrderItemsByProductID(String productID){
        return null;
    }
}
