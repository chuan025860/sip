package com.sip.sipproject.dto;

import com.sip.sipproject.bean.OrderItem;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
public class OrderItemDTO {

    private String orderItemID;

    private String productName;

    private int quantity;

    public static List<OrderItemDTO> convertToOrderItemDTOList(Set<OrderItem> orderItems) {
        List<OrderItemDTO> orderItemDTOs = new ArrayList<>();
        for (OrderItem orderItem : orderItems) {
            OrderItemDTO orderItemDTO = new OrderItemDTO();
            orderItemDTO.setOrderItemID(orderItem.getOrderItemID());
            orderItemDTO.setProductName(orderItem.getRoom().getProductName());
            orderItemDTO.setQuantity(orderItem.getQuantity());
            orderItemDTOs.add(orderItemDTO);
        }
        return orderItemDTOs;
    }

}
