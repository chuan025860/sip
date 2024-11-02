package com.sip.sipproject.bean;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@Entity
@Table(name = "Customer")
public class Customer{

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

    @Column(name = "sex")
    private String sex;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "birthday")
    private Date birthday;

    @Column(name = "phone")
    private String phone;

    @Lob
    @Column(name = "headshot")
    private byte[] headshot;

    @Column(name = "country")
    private String country;

    @Column(name = "city")
    private String city;

    @Column(name = "region")
    private String region;

    @Column(name = "street")
    private String street;

    @Column(name = "postalCode")
    private String postalCode;

    @Column(name = "googleID")
    private String googleID;

    @Column(name = "LineID")
    private String LineID;

    @OneToMany(mappedBy = "customer", fetch = FetchType.EAGER)
    @JsonIgnore
    private Set<OrderTable> orders = new HashSet<>();

    public Customer() {
    }

    public Customer(String email, String password, String customerName, String sex, Date birthday, String phone, String country, String city, String region, String street, String postalCode,String LineID) {
        this.email = email;
        this.password = password;
        this.customerName = customerName;
        this.sex = sex;
        this.birthday = birthday;
        this.phone = phone;
        this.country = country;
        this.city = city;
        this.region = region;
        this.street = street;
        this.postalCode = postalCode;
        this.LineID=LineID;
    }

}