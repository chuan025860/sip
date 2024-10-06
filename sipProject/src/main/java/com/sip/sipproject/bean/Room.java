package com.sip.sipproject.bean;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;
@Data
@EqualsAndHashCode(exclude = {"roomQuantityByDates,roomPictures"})
@Entity
@Table(name = "Room")
public class Room {

	@Id
	@Column(name = "productID")
	private String productID;
	
	@Column(name = "productName")
	private String productName;
	
	@Column(name = "productType")
	private String productType;

	@Column(name = "price")
	private Integer price;

	@Column(name = "state")
	private Boolean state = true;
	
	@Column(name = "capacity")
	private Integer capacity;

	@Column(name = "productDescription")
	private String productDescription;

	@Column(name = "checkInTime")
	private String checkInTime;

	@Column(name = "checkOutTime")
	private String checkOutTime;

	@Column(name = "childExtraBed")
	private Boolean childExtraBed = false;

	@Column(name = "childrenPrice")
	private Integer childrenPrice;

	@Column(name = "age")
	private Integer age;
	
	@Column(name = "privateBathroom")
	private Boolean privateBathroom = false;

	@Column(name = "showerRoom")
	private Boolean showerRoom = false;

	@Column(name = "score")
	private Integer score;

	@Column(name = "createDate", nullable = false, updatable = false)
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date createDate;

	//折扣百分比 搜尋商品須算出
	@Transient
	private double discountPercentage;
	//房間最小數量 搜尋商品須使用
	@Transient
	private Integer minQuantity;

	@OneToMany(fetch = FetchType.LAZY,cascade = CascadeType.ALL,mappedBy = "room")
	private List<RoomPicture> roomPictures;

	@OneToMany(fetch = FetchType.LAZY,cascade = CascadeType.ALL,mappedBy = "room")
	private List<RoomQuantityPriceByDate> roomQuantityByDates;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "hotelID")
	private Hotel hotel;

	@OneToMany(fetch = FetchType.LAZY,mappedBy = "room")
	@JsonIgnore
	private Set<OrderItem> orderItems;

	public Room() {
	}

}
