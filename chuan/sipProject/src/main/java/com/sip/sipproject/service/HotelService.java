package com.sip.sipproject.service;

import com.sip.sipproject.bean.Hotel;
import com.sip.sipproject.bean.HotelDetail;
import com.sip.sipproject.bean.HotelLogin;
import com.sip.sipproject.repository.HotelLoginRepository;
import com.sip.sipproject.repository.HotelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HotelService {
    @Autowired
    private HotelRepository hotelRepository;
    @Autowired
    private HotelLoginRepository hotelloginRepository;

    public List<Hotel> findByHotellogin(HotelLogin hotelLogin) {
        List<Hotel> hotels = hotelRepository.findByHotelLogin(hotelLogin);
        return hotels;

    }

    public Hotel findByHotelId(int hotelID) {
        Optional<Hotel> optional = hotelRepository.findById(hotelID);
        if (optional.isPresent()) {
            Hotel hotel = optional.get();
            return hotel;
        } else {
            return null;
        }
    }

    public void intserHotel(HotelLogin hotelLogin, Hotel hotel, HotelDetail hotelDetail) {
        hotel.setState(true);
        hotelDetail.setMapURL("0");
        hotel.setHotelLogin(hotelLogin);
        hotel.setHotelDetail(hotelDetail);
        hotelDetail.setHotel(hotel);
        hotelRepository.save(hotel);
    }

    public void updateHotelByHotelRequest(Hotel updatehotel, HotelDetail updatehotelDetail) {
        HotelLogin hotelLogin = hotelloginRepository.findByHotel(updatehotel);
        updatehotel.setHotelLogin(hotelLogin);
        updatehotelDetail.setMapURL("0");
        updatehotelDetail.setHotelID(updatehotel.getHotelID());
        //petFriendly開關==false  金額改成null
        if (!updatehotelDetail.getPetFriendly()) {
            updatehotelDetail.setPetDeposit(null);
            updatehotelDetail.setPetCleaningFee(null);
            updatehotel.setHotelDetail(updatehotelDetail);
            updatehotelDetail.setHotel(updatehotel);
        } else {
            updatehotel.setHotelDetail(updatehotelDetail);
            updatehotelDetail.setHotel(updatehotel);
        }
        hotelRepository.save(updatehotel);
    }

    public void updateHotelState(Hotel updatehotel) {
        if (updatehotel.getState()) {
//            HotelLogin hotelLogin = hotelloginRepository.findByHotel(updatehotel);
//            updatehotel.setHotelLogin(hotelLogin);
            updatehotel.setState(false);
            hotelRepository.save(updatehotel);
        } else {
//            HotelLogin hotelLogin = hotelloginRepository.findByHotel(updatehotel);
//            updatehotel.setHotelLogin(hotelLogin);
            updatehotel.setState(true);
            hotelRepository.save(updatehotel);
        }
    }


}