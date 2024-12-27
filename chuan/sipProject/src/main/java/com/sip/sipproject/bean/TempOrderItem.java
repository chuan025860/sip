package com.sip.sipproject.bean;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "TempOrderItem")
@EqualsAndHashCode(exclude = {"tempOrderTable"})
@Data
public class TempOrderItem {
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
    private TempOrderTable tempOrderTable;

}
