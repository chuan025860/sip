package com.sip.sipproject.service;

import com.sip.sipproject.bean.Hotel;
import com.sip.sipproject.bean.HotelDetail;
import com.sip.sipproject.bean.HotelLogin;
import com.sip.sipproject.repository.HotelLoginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class HotelLoginService {
    @Autowired
    private HotelLoginRepository hotelloginRepository;
    @Autowired
    private PasswordEncoder pwdEncoder;

    public void insert(HotelLogin hotelLogin, Hotel hotel, HotelDetail hotelDetail) {
        String encodedPwd = pwdEncoder.encode(hotelLogin.getPassword());
        hotelLogin.setPassword(encodedPwd);
        //存入用Set
        Set<Hotel> hotels = new HashSet<>();
        hotels.add(hotel);
        hotelLogin.setHotels(hotels);
        hotel.setHotelLogin(hotelLogin);
        hotel.setHotelDetail(hotelDetail);
        hotelDetail.setHotel(hotel);
        hotelloginRepository.save(hotelLogin);
    }
    public void updateHotelLogin(HotelLogin hotelLogin) {
        hotelloginRepository.save(hotelLogin);
    }


    public boolean checkIfEmailExist(String email) {
        //檢查email是否存在
        HotelLogin dbHotel = hotelloginRepository.findByEmail(email);
        return dbHotel != null;
    }


    public Boolean resetPwd(String email, String newPwd) {//更新密碼
        HotelLogin dbHotel = hotelloginRepository.findByEmail(email);
        if (dbHotel != null) {
            String encodedPwd = pwdEncoder.encode(newPwd);
            dbHotel.setPassword(encodedPwd);
            hotelloginRepository.save(dbHotel);
            return true;
        }
        return false;
    }

    public HotelLogin checkLogin(String email, String inputPwd) {
        HotelLogin dbHotel = hotelloginRepository.findByEmail(email);
        //比對加密
        if (dbHotel != null) {
            if (pwdEncoder.matches(inputPwd, dbHotel.getPassword())) {
                return dbHotel;
            }
        }
        return null;
    }

    //使用googleID 取得HotelLogin
    public HotelLogin oauth2CheckLogin(String googleID) {//google登入
        return hotelloginRepository.findByGoogleID(googleID);
    }

    //檢查是否有googleID
    public boolean checkGoogleIdExistByEmail(String email) {
        HotelLogin dbHotel = hotelloginRepository.findByEmail(email);
        return dbHotel.getGoogleID() != null;
    }


    //檢查是否有LineID
    public boolean checkLineIdExistByEmail(String email) {
        HotelLogin dbHotel = hotelloginRepository.findByEmail(email);
        return dbHotel.getLineID() != null;
    }


    public HotelLogin findLineID(String LineID){
        return hotelloginRepository.findByLineID(LineID);
    }

    public HotelLogin findByID(Integer loginID){
        Optional<HotelLogin> optional = hotelloginRepository.findById(loginID);
        if (optional.isPresent()) {
            HotelLogin resultLogin = optional.get();
            return resultLogin;
        }else {
            return null;
        }
    }

    public HotelLogin findEmail(String email) {
        //檢查email是否存在
        //並回傳HotelLogin物件
        HotelLogin findEmail = hotelloginRepository.findByEmail(email);
        return findEmail;
    }

}
