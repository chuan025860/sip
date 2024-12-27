package com.sip.sipproject.bean;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@EqualsAndHashCode(exclude = {"hotels","hotelLoginPicture"})
@Table(name = "HotelLogin")
public class HotelLogin {
    @Id
    @Column(name = "loginID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int loginID;

    @OneToOne(fetch = FetchType.LAZY,mappedBy = "hotelLogin", cascade = CascadeType.ALL)
    private HotelLoginPicture hotelLoginPicture;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "hotelLogin", cascade = CascadeType.ALL)
    private Set<Hotel> hotels = new HashSet<>();

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "loginName")
    private String loginName;

    @Column(name = "googleID")
    private String googleID;

    @Column(name = "lineID")
	private String lineID;

    public HotelLogin() {
    }

    public HotelLogin(String email, String password, String loginName, String lineID) {
		this.email = email;
		this.password = password;
		this.loginName=loginName;
		this.lineID=lineID;

	}

}
