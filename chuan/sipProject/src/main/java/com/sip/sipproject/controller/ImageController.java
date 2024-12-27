package com.sip.sipproject.controller;

import com.sip.sipproject.bean.*;
import com.sip.sipproject.security.JwtTokenProvider;
import com.sip.sipproject.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ImageController {
    @Autowired
    HotelLoginService hotelLoginService;
    @Autowired
    CustomerService customerService;
    @Autowired
    HotelLoginPictureService hotelLoginPictureService;
    @Autowired
    DefaultPictureService defaultPictureService;
    @Autowired
    RoomPictureService roomPictureService;
    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @GetMapping("/room/roompicture")
    public ResponseEntity<byte[]> getRoomPicture(@RequestParam String imgID) {
        byte[] imageFile = roomPictureService.findById(Integer.valueOf(imgID)).getImageData();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.IMAGE_JPEG);
        System.out.println("sucess");
        return new ResponseEntity<byte[]>(imageFile, httpHeaders, HttpStatus.OK);
    }

    @GetMapping("/hotel/hotelLoginPicture")
    public ResponseEntity<byte[]> getHotelLoginPicture(HttpSession httpSession) {
        Object hotelAttribute = httpSession.getAttribute("loginId");
        Integer loginId = (Integer) hotelAttribute;
        HotelLogin hotelLogin=hotelLoginService.findByID(loginId);
        HotelLoginPicture hotelLoginPicture = hotelLoginPictureService.findHotelLoginPictureByHotelLogin(hotelLogin);
        if (hotelLoginPicture != null) {
            byte[] imageFile = hotelLoginPicture.getImageData();
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.IMAGE_JPEG);
            return new ResponseEntity<byte[]>(imageFile, httpHeaders, HttpStatus.OK);
        }
        else {
            DefaultPicture defaultPicture = defaultPictureService.findPictureById(1);
            byte[] imageFile = defaultPicture.getImageData();
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.IMAGE_PNG);
            return new ResponseEntity<byte[]>(imageFile, httpHeaders, HttpStatus.OK);
        }
    }
    @GetMapping("/customer/customerPicture")
    public ResponseEntity<byte[]> getCustomerPicture(@CookieValue("Authorization") String token) {
//        Object customerAttribute = httpSession.getAttribute("customerLoginId");
        Integer loginId = Integer.valueOf(jwtTokenProvider.getLoginIdFromToken(token));
        Customer customer=customerService.findByID(loginId);
        if (customer.getHeadshot() != null) {
            byte[] imageFile = customer.getHeadshot();
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.IMAGE_JPEG);
            return new ResponseEntity<byte[]>(imageFile, httpHeaders, HttpStatus.OK);
        }
        else {
            DefaultPicture defaultPicture = defaultPictureService.findPictureById(1);
            byte[] imageFile = defaultPicture.getImageData();
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.IMAGE_JPEG);
            return new ResponseEntity<byte[]>(imageFile, httpHeaders, HttpStatus.OK);
        }
    }

}
