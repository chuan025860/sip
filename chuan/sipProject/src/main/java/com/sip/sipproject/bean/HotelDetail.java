package com.sip.sipproject.bean;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import java.util.Objects;
import java.util.UUID;

@Entity
@EqualsAndHashCode(exclude = {"hotel"})
@Data
@Table(name = "HotelDetail")
public class HotelDetail {
    @GenericGenerator(name = "generator", strategy = "foreign", parameters = @Parameter(name = "property", value = "hotel"))
    @Id
    @Column(name = "hotelID")
    @GeneratedValue(generator = "generator")
    private Integer hotelID;

    @OneToOne(fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn
    private Hotel hotel;

    @Column(name = "mapURL")
    private String mapURL;

    @Column(name = "guiNumber")
    private String guiNumber;

    @Column(name = "businessName")
    private String businessName;

    @Column(name = "openYear")
    private Integer openYear;

    @Column(name = "cleaningService")
    private Boolean cleaningService = false;

    @Column(name = "expressCheckin")
    private Boolean expressCheckin = false;

    @Column(name = "counter24hr")
    private Boolean counter24hr = false;

    @Column(name = "freeWiFi")
    private Boolean freeWiFi = false;

    @Column(name = "roomCard")
    private Boolean roomCard = false;

    @Column(name = "noSmoking")
    private Boolean noSmoking = false;

    @Column(name = "petFriendly")
    private Boolean petFriendly = false;

    @Column(name = "petDeposit")
    private Integer petDeposit;

    @Column(name = "petCleaningFee")
    private Integer petCleaningFee;

    @Column(name = "reservationNotice")
    private String reservationNotice;

    public HotelDetail() {
    }

    public HotelDetail(String mapURL, String guiNumber, String businessName, Integer openYear) {
        this.mapURL = mapURL;
        this.guiNumber = guiNumber;
        this.businessName = businessName;
        this.openYear = openYear;
    }

}