package com.sip.sipproject.service;

import com.sip.sipproject.bean.*;
import com.sip.sipproject.repository.CustomerRepository;
import com.sip.sipproject.repository.OrderTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class OrderTableService {
    @Autowired
    OrderTableRepository orderTableRepository;
    @Autowired
    CustomerRepository customerRepository;

    public Boolean intserOrder(OrderTable orderTable, Set<OrderItem> orderItems) {
        if (orderTable != null && orderItems != null) {
            //一對多  多對一設定
            orderTable.setOrderItems(orderItems);
            for (OrderItem orderItem : orderItems) {
                orderItem.setOrderTable(orderTable);
            }
            orderTableRepository.save(orderTable);
            return true;
        }
        return false;
    }

    public OrderTable findOrderByOrderID_hotel(String orderID, Hotel hotel) {
        OrderTable orderTable = orderTableRepository.findOrderByOrderID_hotel(orderID, hotel);
        return orderTable;

    }

    public OrderTable findOrderByOrderID_customer(String orderID, Customer customer) {
        OrderTable orderTable = orderTableRepository.findOrderByOrderID_customer(orderID, customer);
        return orderTable;

    }

    public List<OrderTable> findOrderDate(Date startDate, Date endDate, Hotel hotel) {

        List<OrderTable> orderTables = orderTableRepository.findOrdersByDateRange(startDate, endDate, hotel);
        return orderTables;
    }

    public List<OrderTable> findOrderByHotelAndStatus(Date currentDate, String Status, Hotel hotel) {
        if (Status.equals("有效") || Status.equals("進行中") || Status.equals("過期")) {
            List<OrderTable> orderTables = orderTableRepository.findOrderByHotelAndStatus(Status, hotel, currentDate);
            return orderTables;
        }
        List<OrderTable> orderTables = orderTableRepository.findOrderByHotelAndStatus("canceled", hotel, currentDate);
        return orderTables;
    }

    public List<OrderTable> upcomingOrders(Date comingDay, Hotel hotel) {
        List<OrderTable> orderTables = orderTableRepository.upcomingOrders(comingDay, hotel);
        return orderTables;
    }

    public List<OrderTable> findOrderByCustomerAndStatus(Date currentDate, String Status, Customer customer) {
        if (Status.equals("有效") || Status.equals("進行中") || Status.equals("過期")) {
            List<OrderTable> orderTables = orderTableRepository.findOrderByCustomerAndStatus(Status, customer, currentDate);
            return orderTables;
        }
        List<OrderTable> orderTables = orderTableRepository.findOrderByCustomerAndStatus("canceled", customer, currentDate);
        return orderTables;
    }

    public List<OrderTable> upcomingOrdersByCustomer(Date comingDay, Customer customer) {
        List<OrderTable> orderTables = orderTableRepository.upcomingOrdersByCustomer(comingDay, customer);
        return orderTables;
    }

    public List<OrderTable> findExpiredOrderDateByCustomer(Date startDate ,Date endDate, Customer customer) {
            List<OrderTable> orderTables = orderTableRepository.findExpiredOrdersByDateRangeByCustomer(startDate,endDate, customer);
            return orderTables;
    }

    public OrderTable updateOrder(OrderTable orderTable) {
        OrderTable newOrderTable = orderTableRepository.save(orderTable);
        return newOrderTable;
    }
//------------------------------------------------------------------------------------------------------

    public List<OrderTable> showOrderByToday(Date startDate, Integer hotelID) {
        System.out.println("進入");
//        List<OrderTable> orderTables=orderTableRepository.findOrderByToday(startDate,hotelID);
        return null;

    }

    public List<OrderTable> showOrderByTodayLoginID(Date startDate, Integer loginID) {
        System.out.println("進入");
//        List<OrderTable> orderTables=orderTableRepository.findOrderByTodayLoginID(startDate,loginID);
        return null;

    }

    public List<OrderTable> findOrderDateByLoginId(Date startDate, Date endDate, Integer loginID) {
        System.out.println("進入");
//        List<OrderTable> orderTables=orderTableRepository.findOrderDateByLoginId(startDate,endDate,loginID);
        return null;
    }
}
