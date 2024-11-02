package com.sip.sipproject.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(exclude = {"room"})
@Entity
@Table(name = "RoomPicture")
public class RoomPicture {

	@Id
	@Column(name = "imgID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer imgID;

	@Column(name = "imageData")
	private byte[] imageData;

	@Transient
	private String base64Image;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "productID")
	private Room room;
}
