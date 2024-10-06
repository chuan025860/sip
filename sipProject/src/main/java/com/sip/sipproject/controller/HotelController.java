package com.sip.sipproject.controller;

import com.sip.sipproject.bean.*;
import com.sip.sipproject.dto.HotelRequest;
import com.sip.sipproject.repository.DefaultPictureRepository;
import com.sip.sipproject.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;


@Controller
public class HotelController {
    @Autowired
    private HotelLoginService hotelloginService;
    @Autowired
    private HotelService hotelService;
    @Autowired
    private HotelLoginService hotelLoginService;
    @Autowired
    private MailService mailService;
    @Autowired
    private HotelLoginPictureService hotelLoginPictureService;
    //導向修改帳號頁
    @GetMapping("/hotel/setaccount")
    public String setlogin(@RequestParam Integer hotelID, HttpSession httpSession, Model model) {
        if (httpSession.getAttribute("loginId") != null && hotelID != null) {//判斷是否已經登入
            httpSession.removeAttribute("resetMail");
            model.addAttribute("hotelID", hotelID);
            return "/hotel/setAccount";
        }
        return "redirect:/hotel/login";
    }

    //修改帳號功能start

    //檢查是否綁定google
    @GetMapping("hotel/checkGoogleID")
    @ResponseBody
    public String checkGoogleID(HttpSession httpSession) {
        String loginEmail = httpSession.getAttribute("loginEmail").toString();
        if (hotelloginService.checkGoogleIdExistByEmail(loginEmail)) {
            System.out.println(loginEmail + " 已綁定");
            return "Y";
        }
        System.out.println(loginEmail + " 未綁定");
        return "N";
    }

    //綁定googleID
    @PutMapping("hotel/bindGoogleID")
    @ResponseBody
    public String bindGoogleID(@RequestBody Map<String, String> googleData, HttpSession httpSession) {

        if (httpSession.getAttribute("loginId").toString() != null) {
            String googleID = googleData.get("sub");
            HotelLogin result = hotelloginService.oauth2CheckLogin(googleID);
            if (result == null) {
                HotelLogin hotelLogin = hotelLoginService.findByID((Integer) httpSession.getAttribute("loginId"));
                hotelLogin.setGoogleID(googleID);
                hotelLoginService.updateHotelLogin(hotelLogin);
                System.out.println(httpSession.getAttribute("loginId").toString() + " 已寫入googleID: " + googleID);
                return "Y";
            }
        }
        return "N";
    }

    //清除googleID
    @DeleteMapping("hotel/clearGoogleID")
    @ResponseBody
    public String clearGoogleID(@RequestParam("loginID") String loginid, HttpSession httpSession) {

        if (httpSession.getAttribute("loginId").toString() != null) {
            HotelLogin hotelLogin = hotelLoginService.findByID((Integer) httpSession.getAttribute("loginId"));
            hotelLogin.setGoogleID(null);
            hotelLoginService.updateHotelLogin(hotelLogin);
            System.out.println(loginid + " 已清除googleID");
            return "Y";
        }
        return "N";
    }

    //檢查是否綁定line
    @GetMapping("hotel/checkLineID")
    @ResponseBody
    public String checkLineID(HttpSession httpSession) {
        String loginEmail = httpSession.getAttribute("loginEmail").toString();
        if (hotelloginService.checkLineIdExistByEmail(loginEmail)) {
            System.out.println(loginEmail + " 已綁定");
            return "Y";
        }
        System.out.println(loginEmail + " 未綁定");
        return "N";
    }

    //驗證用戶_綁定LineID
    @GetMapping("hotel/bindLine_checkPwd")
    public String bindLineID(HttpSession httpSession) {
        Boolean checkLineID = httpSession.getAttribute("lineID") != null;
        Boolean checkEmail = httpSession.getAttribute("hotelLoginEmail") != null;
        if (checkLineID && checkEmail) {
            return "hotel/bindLineCheckPwd";
        } else {
            return "hotel/hotelLogin";
        }
    }

    //修改LineID_綁定LineID
    @PutMapping("/hotel/bindLineID")
    @ResponseBody
    public String checkHotelLogin(@RequestParam("email") String email,
                                  @RequestParam("password") String password,
                                  @RequestParam("lineID") String lineID,
                                  HttpSession httpSession) {
        //檢查登入資訊_新增LineID
        HotelLogin result = hotelloginService.checkLogin(email, password);
        if (result != null) {
            result.setLineID(lineID);
            hotelLoginService.updateHotelLogin(result);
            //刪除全部Session。跳回登入頁 使用LINE登入
            httpSession.invalidate();
            return "Y";
        } else {
            System.out.println(email + " 登入失敗");
            return "N";
        }
    }

    //清除lineID
    @DeleteMapping("hotel/clearLineID")
    @ResponseBody
    public String clearLineID(@RequestParam("loginID") Integer loginid, HttpSession httpSession) {

        if (httpSession.getAttribute("loginId").toString() != null) {
            HotelLogin hotelLogin = hotelLoginService.findByID(loginid);
            hotelLogin.setLineID(null);
            hotelLoginService.updateHotelLogin(hotelLogin);
            System.out.println(loginid + " 已清除LineID");
            return "Y";
        }
        return "N";
    }

    @PostMapping("/hotel/picture")
    @ResponseBody
    public String AddHotelPicture(HttpSession httpSession, @RequestParam MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            // 圖片為空，可以加入對應的錯誤處理邏輯，例如傳回一個錯誤頁面或訊息
            return "N";
        }
        Object hotelAttribute = httpSession.getAttribute("loginId");
        Integer loginId = (Integer) hotelAttribute;
        HotelLogin hotelLogin=hotelLoginService.findByID(loginId);
        hotelLoginPictureService.deleteHotelLoginPictureByHotelLogin(hotelLogin);
        HotelLoginPicture hotelLoginPicture = new HotelLoginPicture();
        hotelLoginPicture.setImageData(file.getBytes());
        hotelLoginPictureService.AddHotelPicture(hotelLogin,hotelLoginPicture);
        return "Y";
    }

    //寄送email成功
    @PostMapping("/hotel/change_password")
    @ResponseBody
    public Map<String, Object> getMailCodeByHotel(@RequestParam("email") String email, @RequestParam("hotelID") Integer hotelID, HttpSession httpSession) {
        Map<String, Object> response = new HashMap<>();
        if (Objects.equals(email, httpSession.getAttribute("loginEmail"))) {//判斷是否為同一個email
            //寄送驗證碼
            mailService.sendResetPwdCodeMail(email, httpSession);
            httpSession.setAttribute("resetMail", email);
            response.put("status", "Y");
            //html-ajax-轉頁hotel/changereset_password/{hotelID}
            response.put("hotelID", hotelID);
        } else {
            response.put("status", "N");
        }
        return response;
    }

    //轉變更密碼頁面
    @GetMapping("/hotel/changereset_password")
    public String changeresetPassword(@RequestParam Integer hotelID, HttpSession httpSession, Model model) {
        //判斷是否已寄送mail
        if (httpSession.getAttribute("resetMail") != null && httpSession.getAttribute("loginId") != null && hotelID != null) {
            model.addAttribute("hotelID", hotelID);
            return "hotel/changePwd";
        } else if //判斷有無resetMail 沒有轉回setaccount
        (httpSession.getAttribute("resetMail") == null && httpSession.getAttribute("loginId") != null) {
            return "redirect:/hotel/setaccount?hotelID=" + hotelID;
        }
        return "redirect:/hotel/login";
    }

    //更改密碼
    @PutMapping("/hotel/changereset_password")
    @ResponseBody
    public String postResetPassword(@RequestParam("newPwd") String newPwd,
                                    @RequestParam("verificationCode") String verificationCode,
                                    HttpSession httpSession) {

        String resetMail = httpSession.getAttribute("resetMail").toString();
        String sessionCode = httpSession.getAttribute("verificationCode").toString();
        System.out.println("---------------------------" + newPwd);
        System.out.println("更新Pwd Email: " + resetMail);
        System.out.println("Session驗證碼: " + sessionCode);
        System.out.println("輸入驗證碼: " + verificationCode);

        if (Objects.equals(verificationCode, sessionCode)) {
            Boolean pwdResult = hotelLoginService.resetPwd(resetMail, newPwd);
            if (pwdResult) {
                httpSession.removeAttribute("resetMail");
                httpSession.removeAttribute("verificationCode");
                System.out.println(resetMail + "驗證碼已刪除");
                mailService.sendMail(resetMail, "密碼變更成功", "帳號: " + resetMail + "\n密碼已變更" + "\n變更時間: " + new Date());
                httpSession.removeAttribute("loginId");
                return "Y";
            } else {
                return "errMsg";}
        } else {
            return "N";
        }
    }

    //重新寄送驗證碼
    @GetMapping("/hotel/resendVerificationCode_password")
    public String resendVerificationCode(@RequestParam Integer hotelID, HttpSession httpSession, Model model) {
        //判斷是否登入、hotelID
        if (httpSession.getAttribute("loginId") != null && hotelID != null) {
            //model裝入hotelID
            model.addAttribute("hotelID", hotelID);
            String email = hotelService.findByHotelId(hotelID).getHotelLogin().getEmail();
            mailService.sendResetPwdCodeMail(email, httpSession);
            httpSession.setAttribute("resetMail", email);
            return "hotel/changePwd";
        } else if (httpSession.getAttribute("resetMail") == null && httpSession.getAttribute("loginId") != null) {
            return "redirect:/hotel/setaccount?hotelID=" + hotelID;
        }
        return "redirect:/hotel/login";
    }
    //修改帳號功能end

    //導向飯店詳細資料查詢頁
    @GetMapping("/hotel/hoteldetail")
    public String hoteldetail(@RequestParam("hotelID") Integer hotelID, Model model, HttpSession httpSession) {
        if (httpSession.getAttribute("loginId") == null || hotelID == null) {
            return "redirect:/hotel/login";
        }
        Hotel hotel = hotelService.findByHotelId(hotelID);
        HotelDetail hotelDetail = hotel.getHotelDetail();
        model.addAttribute("hotel", hotel);
        model.addAttribute("hotelDetail", hotelDetail);
        model.addAttribute("hotelID",hotelID);
        return "/hotel/findHotelDetail";
    }

    //導向更新飯店頁
    @PostMapping("/hotel/updatehotel")
    public String selectByHotelId(@RequestParam("hotelID") Integer hotelID, Model model, HttpSession httpSession) {
        if (httpSession.getAttribute("loginId") == null || hotelID == null) {
            return "redirect:/hotel/login";
        }

        Hotel hotel = hotelService.findByHotelId(hotelID);
        HotelDetail hotelDetail = hotel.getHotelDetail();
        model.addAttribute("hotel", hotel);
        model.addAttribute("hotelDetail", hotelDetail);
        model.addAttribute("hotelID",hotelID);
        return "/hotel/updatehotel";
    }

    //更新飯店、飯店詳細資料
    @PutMapping("/hotel/update")
    @ResponseBody
    public String updateHotelByHotelRequest(@RequestBody HotelRequest request,HttpSession httpSession) {
        Hotel updatehotel = request.getHotel();
        HotelDetail updatehotelDetail = request.getHotelDetail();
        hotelService.updateHotelByHotelRequest(updatehotel, updatehotelDetail);
        httpSession.setAttribute("hotel"+updatehotel.getHotelID(),updatehotel.getHotelName());
        return "Y";
    }

    //  啟用/停用飯店
    @PutMapping("/hotel/updateHotelState")
    @ResponseBody
    public String updateHotelState(@RequestBody HotelRequest requestData) {
        Integer hotelID = requestData.getHotelID();
        hotelService.updateHotelState(hotelService.findByHotelId(hotelID));
        return"Y";

    }

    //導向新增飯店頁
    @GetMapping("/hotel/add")
    public String addHotel(@RequestParam("hotelID") Integer hotelID,HttpSession httpSession, Model model) {
        if (httpSession.getAttribute("loginId") == null || hotelID == null) {
            return "redirect:/hotel/login";
        }
        model.addAttribute("hotelID",hotelID);
        return "/hotel/addhotel";
    }
    //新增飯店頁
    @PostMapping("/hotel/addHotel")
    @ResponseBody
    public String postAddHotel(@RequestBody HotelRequest request,HttpSession httpSession) {
        HotelLogin hotelLogin=hotelLoginService.findByID((Integer)httpSession.getAttribute("loginId"));
        Hotel hotel = request.getHotel();
        HotelDetail hotelDetail = request.getHotelDetail();
        hotelService.intserHotel(hotelLogin,hotel, hotelDetail);
        return "Y";
    }

    //預覽飯店頁
    @GetMapping("/hotel/preview")
    public String previewHotel(@RequestParam("hotelID") Integer hotelID,HttpSession httpSession,Model model) {
        if (httpSession.getAttribute("loginId") == null || hotelID == null) {
            return "redirect:/hotel/login";
        }
        Hotel hotel = hotelService.findByHotelId(hotelID);
        hotel.setHotelIntroduction(hotel.getHotelIntroduction().replace("\n", "<br/>"));
        model.addAttribute("hotelID",hotelID);
        model.addAttribute("hotel",hotel);
        return "/hotel/previewHotel";
    }

}
