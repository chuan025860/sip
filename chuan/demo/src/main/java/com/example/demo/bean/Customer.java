package com.example.demo.bean;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@Entity
@Table(name = "Customer")
public class Customer {
    @Id
    @Column(name = "loginID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer loginID;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "CustomerName")
    private String customerName;


    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "birthday")
    private Date birthday;

    @Column(name = "phone")
    private String phone;

    @Lob
    @Column(name = "headshot")
    private byte[] headshot;


    @Column(name = "city")
    private String city;

    @Column(name = "street")
    private String street;


    public Customer() {
    }

    public Customer(String email, String password, String customerName, String sex, Date birthday, String phone, String country, String city, String region, String street, String postalCode, String LineID) {
        this.email = email;
        this.password = password;
        this.customerName = customerName;
        this.birthday = birthday;
        this.phone = phone;
        this.street = street;
    }

}