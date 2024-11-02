package com.sip.sipproject.bean;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "HotelLoginPicture")
public class HotelLoginPicture {
    @Id
    @Column(name = "hotelLoginImgID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer hotelLoginImgID;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loginID") // 关联到 HotelLogin 的主键字段
    private HotelLogin hotelLogin;

    @Lob
    @Column(name = "imageData")
    private byte[] imageData;
}
