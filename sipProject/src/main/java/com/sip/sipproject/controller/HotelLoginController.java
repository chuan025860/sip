package com.sip.sipproject.controller;

import com.sip.sipproject.bean.Hotel;
import com.sip.sipproject.bean.HotelDetail;
import com.sip.sipproject.bean.HotelLogin;
import com.sip.sipproject.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;


@Controller
public class HotelLoginController {
    @Autowired
    private HotelLoginService hotelloginService;
    @Autowired
    private MailService mailService;
    @Autowired
    private HotelService hotelService;


    //登入頁
    @GetMapping("/hotel/login")
    public String hotelLoginPage(HttpSession httpSession) {
        //初始化註冊信箱session
        httpSession.removeAttribute("registerMail");
        //判斷是否已經登入
        if (httpSession.getAttribute("loginId") != null) {
            return "redirect:/hotel/chooseHotel";
        }
        return "hotel/hotelLogin";
    }


    // 登入判斷
    @PostMapping("/hotel/login")
    @ResponseBody
    public String checkHotelLogin(@RequestParam("email") String email,
                                  @RequestParam("password") String password,
                                  HttpSession httpSession) {
        //檢查登入資訊
        HotelLogin result = hotelloginService.checkLogin(email, password);
        if (result != null) {
            httpSession.setAttribute("loginName", result.getLoginName());
            httpSession.setAttribute("loginEmail", result.getEmail());
            httpSession.setAttribute("loginId", result.getLoginID());
            return "Y";
        } else {
            System.out.println(email + " 登入失敗");
            return "N";
        }
    }

    //轉選擇飯店頁
    @GetMapping("/hotel/chooseHotel")
    public String chooseHotel(HttpSession httpSession, Model model) {
        if (httpSession.getAttribute("loginId") != null) {//判斷是否已經登入
            HotelLogin hotelLogin=hotelloginService.findByID((Integer) httpSession.getAttribute("loginId"));
            List<Hotel> hotels = hotelService.findByHotellogin(hotelLogin);
            model.addAttribute("hotels", hotels);
            httpSession.setAttribute("hotels",hotels);
            return "hotel/chooseHotel";
        }
        //都不是=>轉回登入頁
        return "redirect:/hotel/login";
    }

    //轉後台頁
    @GetMapping("/hotel/dashboard")
    public String hotelDashboardPage(HttpSession httpSession, Model model, @RequestParam Integer hotelID) {
        if (httpSession.getAttribute("loginId") != null && hotelID != null) {//判斷是否已經登入
            Hotel selectedHotel = hotelService.findByHotelId(hotelID);
            //backStageNavbar_2.html 使用session "hotel"+hotelID 取得hotelName
            httpSession.setAttribute("hotel"+hotelID,selectedHotel.getHotelName());
            model.addAttribute("hotelID", hotelID);
            return "hotel/hotelBackIndex";
        }
        return "redirect:/hotel/login";
    }

    //Google 登入
    @PostMapping("/hotel/google/login/chooseHotel")
    @ResponseBody
    public String hotelDashboardPage(@RequestBody Map<String, String> googleData, HttpSession httpSession) {
        if (googleData != null) {//判斷是否為Google登入
            String googleID = googleData.get("sub");
            String googleEmail = googleData.get("email");
            // String googleName = googleData.get("name");
            HotelLogin result = hotelloginService.oauth2CheckLogin(googleID);

            if (result != null) {// 有帳號=>後台頁
                httpSession.setAttribute("loginId", result.getLoginID());
                httpSession.setAttribute("loginEmail", result.getEmail());
                httpSession.setAttribute("loginName", result.getLoginName());
                System.out.println("GoogleLogin: " + googleEmail + " GoogleID: " + googleID);
                System.out.println(result.getLoginID() + "-" + result.getLoginName() + " Login Success !!");
                return "Y";
            } else {// 未有帳號=>註冊頁
                System.out.println("GoogleLogin Fail!!" + " GoogleID: " + googleID);
                //model.addAttribute("errMsg", "此Google帳號尚未綁定<br>請先註冊或登入綁定");
                return "N";
            }
        }
        return "N";//都不是=>登入頁
    }


    //登出(刪除全部Session)
    @GetMapping("/hotel/logout")
    public String hotelLogout(HttpSession httpSession) {
        if (httpSession != null) {
            httpSession.invalidate();
            return "redirect:/hotel/login";
        }
        return "hotel/hotelLogin";
    }


    //註冊開始
    //1.轉註冊mail驗證頁
    @GetMapping("/hotel/startregister")
    public String registerCheckEmail(HttpSession httpSession) {
        //移除 未驗證註冊信箱、驗證碼
        httpSession.removeAttribute("verificationCode");
        httpSession.removeAttribute("registerChkMail");
        return "hotel/registerCheckEmail";
    }

    //2.註冊前寄送驗證碼
    @PostMapping("/hotel/startregister")
    @ResponseBody
    public String getMailCodeForRegister(@RequestParam("email") String email, HttpSession httpSession) {
        //判斷信箱是否已註冊
        if (!hotelloginService.checkIfEmailExist(email)) {
            //寄送驗證碼
            mailService.sendRegisterPwdCodeMail(email, httpSession);
            httpSession.setAttribute("registerChkMail", email);//未驗證信箱存入session
            return "Y";
        } else {
            return "N";
        }
    }

    //3-1.註冊前比對Email驗證碼(連結)
    @GetMapping("/hotel/registerCode")
    public String checkRegisterCodeForLink(@RequestParam("verificationCode") String verificationCode, HttpSession
            httpSession, Model model) {
        if (httpSession.getAttribute("verificationCode") != null) {
            String registerChkMail = httpSession.getAttribute("registerChkMail").toString();//取得未驗證信箱
            String sessionCode = httpSession.getAttribute("verificationCode").toString();

            System.out.println("Session註冊驗證碼: " + sessionCode);
            System.out.println("輸入註冊驗證碼: " + verificationCode);

            if (Objects.equals(verificationCode, sessionCode)) {//比對驗證碼
                httpSession.removeAttribute("verificationCode");//移除 驗證碼
                httpSession.setAttribute("registerMail", registerChkMail);//已驗證信箱存入session
                httpSession.removeAttribute("registerChkMail");//移除 未驗證註冊信箱
                return "redirect:/hotel/register";
            }
        }
        model.addAttribute("errMsg", "驗證碼錯誤或失效");
        return "hotel/registerCheckEmail";
    }

    //3-2.註冊前比對Email驗證碼
    @PostMapping("/hotel/registerCode")
    @ResponseBody
    public String checkRegisterCode(@RequestParam("verificationCode") String verificationCode,
                                    HttpSession httpSession) {

        if (httpSession.getAttribute("verificationCode") != null) {
            String registerChkMail = httpSession.getAttribute("registerChkMail").toString();//取得未驗證信箱
            String sessionCode = httpSession.getAttribute("verificationCode").toString();

            System.out.println("Session註冊驗證碼: " + sessionCode);
            System.out.println("輸入註冊驗證碼: " + verificationCode);

            if (Objects.equals(verificationCode, sessionCode)) {//比對驗證碼
                httpSession.removeAttribute("verificationCode");//移除 驗證碼
                httpSession.setAttribute("registerMail", registerChkMail);//已驗證信箱存入session
                httpSession.removeAttribute("registerChkMail");//移除 未驗證註冊信箱
                return "Y";
            }else {
                //驗證碼錯誤 返回
                return "N";
            }
        }
        //驗證碼錯誤 返回
        return "N";
    }

    //4.轉註冊頁
    @GetMapping("/hotel/register")
    public String hotelRegisterPage(HttpSession httpSession) {
        if (httpSession.getAttribute("registerMail") != null) {
            return "hotel/hotelCreatAccount";
        }
        return "redirect:/hotel/startRegister";
    }

    //5.註冊頁
    @PostMapping("/hotel/register")
    public String postHotellogin(@RequestParam("email") String email,
                                 @RequestParam("pass") String pass,
                                 @RequestParam("loginName") String loginName,
                                 @RequestParam("hotelName") String hotelName,
                                 @RequestParam("hotelType") String hotelType,
                                 @RequestParam("hotelStar") String hotelStar,
                                 @RequestParam("tel") String tel,
                                 @RequestParam("country") String country,
                                 @RequestParam("city") String city,
                                 @RequestParam("region") String region,
                                 @RequestParam("street") String street,
                                 @RequestParam("postalCode") String postalCode,
                                 @RequestParam("guiNumber") String guiNumber,
                                 @RequestParam("businessName") String businessName,
                                 @RequestParam("openYear") String openYear,
                                 @RequestParam("LineID") String LineID,
                                 @RequestParam("hotelIntroduction") String hotelIntroduction,
                                 HttpSession httpSession) {
        if (LineID.equals("")){
            LineID=null;
        }
        String mapURL = "0";
        String state = "true";
        HotelLogin hotelLogin = new HotelLogin(email, pass, loginName, LineID);
        Hotel hotel = new Hotel(hotelName, hotelType, hotelStar, tel, country, city, region, street, postalCode, Boolean.parseBoolean(state), hotelIntroduction);
        HotelDetail hotelDetail = new HotelDetail(mapURL, guiNumber, businessName, Integer.parseInt(openYear));

        if (Objects.equals(httpSession.getAttribute("registerMail").toString(), email)) {//比對session中已驗證信箱與輸入之信箱
            hotelloginService.insert(hotelLogin, hotel, hotelDetail);
            mailService.sendMail(email, "已完成飯店註冊",
                    "您的帳號: " + email +
                            "\n公司名稱: " + loginName +
                            "\n飯店名稱: " + hotelName +
                            "\n註冊時間: " + new Date());
            return "redirect:/hotel/login";
        } else {
            System.out.println("註冊失敗，信箱未經過驗證");
            return "hotel/hotelCreatAccount";
        }
    }


    //重設密碼-開始
    //1.轉忘記密碼
    @GetMapping("/hotel/forget_password")
    public String forgetPassword() {
        return "hotel/forgetPwd";
    }

    //2.寄送驗證碼
    @PostMapping("/hotel/forget_password")
    @ResponseBody
    public String getMailCodeByHotel(@RequestParam("email") String email, HttpSession httpSession) {

        if (hotelloginService.checkIfEmailExist(email)) {//判斷信箱是否已註冊
            //寄送驗證碼
            mailService.sendResetPwdCodeMail(email, httpSession);
            httpSession.setAttribute("resetMail", email);
            return "Y";
        } else {
            return "N";
        }
    }

    //3.轉重設密碼頁面
    @GetMapping("/hotel/reset_password")
    public String resetPassword(HttpSession httpSession) {
        //判斷是否已寄送mail
        if (httpSession.getAttribute("resetMail") != null) {
            return "hotel/resetPwd";
        }
        return "redirect:/hotel/forget_password";
    }

    //重設密碼-結束
    //4.密碼重設
    @PutMapping("/hotel/reset_password")
    public String postResetPassword(@RequestParam("newPwd") String newPwd,
                                    @RequestParam("verificationCode") String verificationCode,
                                    HttpSession httpSession,
                                    Model model) {

        String resetMail = httpSession.getAttribute("resetMail").toString();
        String sessionCode = httpSession.getAttribute("verificationCode").toString();
        if (Objects.equals(verificationCode, sessionCode)) {//比對驗證碼
            Boolean pwdResult = hotelloginService.resetPwd(resetMail, newPwd);//更新密碼
            if (pwdResult) {
                httpSession.removeAttribute("resetMail");
                httpSession.removeAttribute("verificationCode");
                mailService.sendMail(resetMail, "密碼變更成功!!!", "您的帳號: " + resetMail + "\n密碼已變更" + "\n變更時間: " + new Date());//寄送密碼變更通知
                return "redirect:/hotel/login";
            } else {
                model.addAttribute("errMsg", "驗證碼正確,密碼更新失敗");
                return "hotel/resetPwd";
            }
        } else {
            model.addAttribute("errMsg", "驗證碼錯誤");
            return "hotel/resetPwd";
        }
    }

    //line登入
    @GetMapping("/hotel/lineLogin")
    public String lineLogin() {
        return "hotel/lineLogin";
    }

    @PostMapping("/hotel/getLineInformation")
    public ResponseEntity<?> handleLineLogin(@RequestParam("code") String code) {
        String tokenUrl = "https://api.line.me/oauth2/v2.1/token";

        // 構建交換授權碼的請求數據
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("code", code);
        params.add("redirect_uri", "http://localhost:8080/sip/hotel/lineLogin");  // 一定要與 LINE Console 中設定的相同
        params.add("client_id", "2000911017");
        params.add("client_secret", "7389ef57adffa0ac4b1f046be56f74ce");

        // 使用 RestTemplate 發送 POST 請求
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            // 發送 POST 請求到 LINE 伺服器
            ResponseEntity<Map> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                // 成功交換 access_token
                // 獲取 id_token
                Map<String, Object> responseBody = response.getBody();
                String idToken = (String) responseBody.get("id_token");

                // 將 id_token 回傳給前端
                Map<String, String> result = new HashMap<>();
                result.put("id_token", idToken);
                return ResponseEntity.ok(result);
            } else {
                // 處理交換過程中的錯誤
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to exchange authorization code");
            }
        } catch (RestClientException e) {
            // 處理異常
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during token exchange");
        }
    }

    //Line登入判斷
    @PostMapping("/hotel/createline")
    @ResponseBody
    public String createline(@RequestParam("aud") String lineID,
                             @RequestParam("email") String email,
                             HttpSession httpSession) {
        System.out.println(lineID);
        System.out.println(email);
        HotelLogin hotelLogin = hotelloginService.findEmail(email);
        HotelLogin result = hotelloginService.findLineID(lineID);
        //無email、LineID 轉註冊頁
        if (result == null && hotelLogin == null) {
            httpSession.setAttribute("LineID", lineID);
            httpSession.setAttribute("registerMail", email);
            return "N";
        } else if (result == null && hotelLogin != null)  //有email、無LineID 轉綁定Line頁
        {
            httpSession.setAttribute("lineID", lineID);
            httpSession.setAttribute("hotelLoginEmail", hotelLogin.getEmail());
            return "bindLineID";
        } else //都有完成都入
        httpSession.setAttribute("loginName", result.getLoginName());
        httpSession.setAttribute("loginEmail", result.getEmail());
        httpSession.setAttribute("loginId", result.getLoginID());
        System.out.println(result.getLoginID() + "-" + result.getLoginName() + " Login Success !!");
        return "Y";
    }



}


