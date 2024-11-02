package com.sip.sipproject.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class OrderDTO {

    private String orderID;
    private String lastName;
    private String firstName;
    private Date checkInDate;
    private Date checkOutDate;
    private Integer orderPrice;
    private String payment;
    private String orderStatus;
    private List<OrderItemDTO> orderItems;
    public OrderDTO() {
    }

    public OrderDTO(String orderID, String lastName, String firstName, Date checkInDate, Date checkOutDate, Integer orderPrice, String payment, String orderStatus, List<OrderItemDTO> orderItems) {
        this.orderID = orderID;
        this.lastName = lastName;
        this.firstName = firstName;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.orderPrice = orderPrice;
        this.payment = payment;
        this.orderStatus = orderStatus;
        this.orderItems = orderItems;
    }

//    public static OrderDTO findOrder(OrderTable orderTable, List<OrderItem> orderItems) {
//        OrderDTO orderDTO = new OrderDTO();
//        orderDTO.setOrderTable(orderTable);
//        OrderItemDTO orderItemDTO = new OrderItemDTO();
//        List<OrderItemDTO> orderItemDTOs = orderItemDTO.convertToOrderItemDTOList(orderItems);
////        orderDTO.setOrderItems(orderItemDTOs);
//        return orderDTO;
//    }
}
