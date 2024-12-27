package com.sip.sipproject.bean;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "OrderItem")
@EqualsAndHashCode(exclude = {"orderTable"})
@Data
public class OrderItem {
    @Id
    @Column(name = "orderItemID")
    private String orderItemID;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "orderItemPrice")
    private int orderItemPrice;

    @ManyToOne
    @JoinColumn(name = "productID")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "orderID")
    private OrderTable orderTable;

}
