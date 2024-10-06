package com.sip.sipproject.repository;

import com.sip.sipproject.bean.HotelLogin;
import com.sip.sipproject.bean.HotelLoginPicture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface HotelLoginPictureRepository extends JpaRepository<HotelLoginPicture, Integer> {
    @Query(value="from HotelLoginPicture where hotelLogin = :hotelLogin")
    HotelLoginPicture findHotelLoginPicturByHotelLogin(@Param("hotelLogin") HotelLogin hotelLogin);


    @Query("DELETE FROM HotelLoginPicture  where hotelLogin = :hotelLogin")
    @Modifying
    @Transactional
    void deleteHotelLoginPicturByHotelLogin(@Param("hotelLogin") HotelLogin hotelLogin);
}
