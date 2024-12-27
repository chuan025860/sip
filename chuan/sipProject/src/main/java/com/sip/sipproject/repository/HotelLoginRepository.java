package com.sip.sipproject.repository;

import com.sip.sipproject.bean.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sip.sipproject.bean.HotelLogin;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface HotelLoginRepository extends JpaRepository<HotelLogin, Integer> {
    @Query("FROM HotelLogin WHERE email = :email")
    HotelLogin findByEmail(@Param("email") String email);

    @Query("FROM HotelLogin WHERE lineID = :lineID")
    HotelLogin findByLineID(@Param("lineID") String lineID);
    @Query("FROM HotelLogin WHERE googleID = :googleID")
    HotelLogin findByGoogleID(@Param("googleID") String googleID);

    @Query("FROM HotelLogin WHERE :hotel MEMBER OF hotels")
    HotelLogin findByHotel(@Param("hotel") Hotel hotel);

}
