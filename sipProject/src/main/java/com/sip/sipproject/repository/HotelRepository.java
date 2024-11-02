package com.sip.sipproject.repository;

import java.util.List;

import com.sip.sipproject.bean.HotelLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sip.sipproject.bean.Hotel;

public interface HotelRepository extends JpaRepository<Hotel, Integer> {
	
	@Query("FROM Hotel h WHERE h.hotelLogin = :hotelLogin")
    List<Hotel> findByHotelLogin(@Param("hotelLogin") HotelLogin hotelLogin);
	
	
}
