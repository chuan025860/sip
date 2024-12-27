package com.sip.sipproject.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;


@Entity
@Table(name = "RoomQuantityPriceByDate")
@Data
@EqualsAndHashCode(exclude = {"room"})
public class RoomQuantityPriceByDate {
    @Id
    @Column(name = "roomQuantityID")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String  roomQuantityID;

    @Column(name = "quantityByDate")
    private Integer quantityByDate;

    @Column(name = "price")
    private Integer price;

    @Column(name = "date")
    private Date date;

    @Column(name = "discountPrice")
    private Integer discountPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productID")
    private Room room;
}
