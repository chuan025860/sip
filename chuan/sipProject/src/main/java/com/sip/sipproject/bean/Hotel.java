package com.sip.sipproject.bean;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Data
@EqualsAndHashCode(exclude = {"hotelLogin","hotelDetail","Rooms","orderTables","tempOrderTables"})
@Table(name = "hotel")
public class Hotel {
	@Id
	@Column(name = "hotelID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer hotelID;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "loginID")
	private HotelLogin hotelLogin;

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "hotel", cascade = CascadeType.ALL)
	private HotelDetail hotelDetail;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "hotel", cascade = CascadeType.ALL)
	private Set<Room> Rooms;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "hotel", cascade = CascadeType.ALL)
	private Set<OrderTable> orderTables;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "hotel", cascade = CascadeType.ALL)
	private Set<TempOrderTable> tempOrderTables;

	@Column(name = "hotelName")
	private String hotelName;

	@Column(name = "hotelType")
	private String hotelType;

	@Column(name = "hotelStar")
	private String hotelStar;

	@Column(name = "tel")
	private String tel;

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

	@Column(name = "state")
	private Boolean state;

	@Column(name = "hotelIntroduction")
	private String hotelIntroduction;

	public Hotel() {
	}

	public Hotel(String hotelName, String hotelType, String hotelStar, String tel, String country, String city,
				 String region, String street, String postalCode, Boolean state, String hotelIntroduction) {
		this.hotelName = hotelName;
		this.hotelType = hotelType;
		this.hotelStar = hotelStar;
		this.tel = tel;
		this.country = country;
		this.city = city;
		this.region = region;
		this.street = street;
		this.postalCode = postalCode;
		this.state = state;
		this.hotelIntroduction = hotelIntroduction;
	}
}