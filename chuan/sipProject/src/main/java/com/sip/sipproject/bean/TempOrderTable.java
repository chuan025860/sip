package com.sip.sipproject.bean;


import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "TempOrderTable")
@Data
public class TempOrderTable {
    @Id
    @Column(name = "orderID")
    private String orderID;

    //用checkinTime判斷 有效、進行中的訂單
    //如果column狀態為canceled 不需要使用checkinTime判斷
    @Column(name = "orderStatus")
    private String orderStatus = "valid";

    @Column(name = "lastName")
    private String lastName;

    @Column(name = "firstName")
    private String firstName;

    @Column(name = "email")
    private String email;

    @Column(name = "tel")
    private String tel;

    @Column(name = "orderTime")
    private String orderTime;

    @Column(name = "updateTime")
    private String updateTime;

    @Column(name = "orderPrice")
    private Integer orderPrice;

    @Column(name = "payment")
    private String payment;

    @Column(name = "numPeople")
    private Integer numPeople;

    @Column(name = "checkInDate")
    private Date checkInDate;

    @Column(name = "checkOutDate")
    private Date checkOutDate;

    @Column(name = "evaluation")
    private String evaluation;

    @Column(name = "comment")
    private String comment;

    @ManyToOne
    @JoinColumn(name = "loginID")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "hotelID")
    private Hotel hotel;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "tempOrderTable", cascade = CascadeType.ALL)
    private Set<TempOrderItem> tempOrderItems = new HashSet<>();

}