package com.sip.sipproject.service;

import com.sip.sipproject.bean.HotelLogin;
import com.sip.sipproject.bean.HotelLoginPicture;
import com.sip.sipproject.repository.HotelLoginPictureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HotelLoginPictureService {
    @Autowired
    HotelLoginPictureRepository hotelLoginPictureRepository;

    public void AddHotelPicture(HotelLogin hotelLogin,HotelLoginPicture hotelLoginPicture){
        hotelLoginPicture.setHotelLogin(hotelLogin);
        hotelLoginPictureRepository.save(hotelLoginPicture);

    }
    public HotelLoginPicture findHotelLoginPictureByHotelLogin(HotelLogin hotelLogin){
      return   hotelLoginPictureRepository.findHotelLoginPicturByHotelLogin(hotelLogin);
    }

    public void deleteHotelLoginPictureByHotelLogin(HotelLogin hotelLogin){
        hotelLoginPictureRepository.deleteHotelLoginPicturByHotelLogin(hotelLogin);
    }

}
