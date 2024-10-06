package com.sip.sipproject.controller;

import com.sip.sipproject.bean.Customer;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Controller
public class CustomerLoginController {
    @Autowired
    CustomerService customerService;
    @Autowired
    MailService mailService;


    //登入頁
    @GetMapping("/customer/login")
    public String customerLoginPage(HttpSession httpSession) {
        //初始化註冊session
        httpSession.removeAttribute("customerRegisterMail");
        //判斷是否已經登入
        if (httpSession.getAttribute("customerLoginId") != null) {
            return "redirect:/customer/dashboard";
        }
        return "customer/customerLogin";
    }

    // 登入判斷
    @PostMapping("/customer/login")
    @ResponseBody
    public String checkCustomerLogin(@RequestParam("email") String email,
                                     @RequestParam("password") String password,
                                     HttpSession httpSession) {

        Customer customer = customerService.checkLogin(email, password);
        if (customer != null) {
            httpSession.setAttribute("customerloginName", customer.getCustomerName());
            httpSession.setAttribute("customerloginEmail", customer.getEmail());
            httpSession.setAttribute("customerLoginId", customer.getLoginID());
            httpSession.setAttribute("userLoggedIn", true);
            return "Y";
        } else {
            System.out.println(email + " 登入失敗");
        }
        return "N";
    }

    //轉會員頁
    @GetMapping("/customer/dashboard")
    public String customerDashboardPage(HttpSession httpSession, Model model) {
       if (httpSession.getAttribute("customerLoginId") != null) {//判斷是否已經登入
            return "customer/customerIndex";
        }
        return "redirect:/customer/login";//都不是=>登入頁
    }

    //登出(刪除全部Session)
    @GetMapping("/customer/logout")
    public String customerLogout(HttpSession httpSession) {
        if (httpSession != null) {
            httpSession.invalidate();
            return "redirect:/customer/login";
        }
        return "customer/customerlogin";
    }

    //首頁登出(刪除全部Session)
    @GetMapping("/sipIndex/logout")
    public String indexLogout(HttpSession httpSession) {
        if (httpSession != null) {
            httpSession.invalidate();
            return "redirect:/sipIndex";
        }
        return "sip/sipIndex";
    }

    //註冊開始
    //1.轉註冊mail驗證頁
    @GetMapping("/customer/startRegister")
    public String registerCheckEmail(HttpSession httpSession) {
        httpSession.removeAttribute("verificationCode");
        httpSession.removeAttribute("registerChkMail");
        return "customer/registerCheckEmail";
    }

    //2.檢查email發認證信
    @PostMapping("/customer/startRegister")
    @ResponseBody
    public String getMailCodeForRegister(@RequestParam("email") String email, HttpSession httpSession) {
        if (!customerService.checkCustomerByEmail(email)) {//判斷信箱是否已註冊
            //寄送驗證碼
            mailService.sendRegisterPwdCodeMailForCustomer(email, httpSession);
            httpSession.setAttribute("registerChkMail", email);//未驗證信箱存入session
            return "Y";
        } else {
            return "N";
        }
    }

    //3-1.註冊前比對Email驗證碼(連結)
    @GetMapping("/customer/registerCode")
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
                return "redirect:/customer/register";
            }
        }
        model.addAttribute("errMsg", "驗證碼錯誤或失效");
        return "hotel/registerCheckEmail";
    }

    //3-2.註冊前比對Email驗證碼
    @PostMapping("/customer/registerCode")
    @ResponseBody
    public String checkRegisterCodeForLink(@RequestParam("verificationCode") String verificationCode,
                                           HttpSession httpSession) {

        if (httpSession.getAttribute("verificationCode") != null) {
            String registerChkMail = httpSession.getAttribute("registerChkMail").toString();//取得未驗證信箱
            String sessionCode = httpSession.getAttribute("verificationCode").toString();

            System.out.println("Session註冊驗證碼: " + sessionCode);
            System.out.println("輸入註冊驗證碼: " + verificationCode);

            if (Objects.equals(verificationCode, sessionCode)) {//比對驗證碼
                httpSession.removeAttribute("verificationCode");
                httpSession.setAttribute("registerMail", registerChkMail);//已驗證信箱存入session
                httpSession.removeAttribute("registerChkMail");
                System.out.println(registerChkMail + "註冊驗證碼已刪除");
                return "Y";
            }
        }
        return "N";
    }

    //4.轉註冊頁
    @GetMapping("/customer/register")
    public String customerRegisterPage(HttpSession httpSession) {
        if (httpSession.getAttribute("registerMail") != null) {
            return "customer/customerCreatAccount";
        }
        return "redirect:/customer/startRegister";
    }

    //5.註冊頁
    @PostMapping("/customer/register")
    public String postHotellogin(@RequestParam("email") String email,
                                 @RequestParam("pass") String pass,
                                 @RequestParam("CustomerName") String CustomerName,
                                 @RequestParam("birthday") String birthday,
                                 @RequestParam("sex") String sex,
                                 @RequestParam("country") String country,
                                 @RequestParam("city") String city,
                                 @RequestParam("region") String region,
                                 @RequestParam("street") String street,
                                 @RequestParam("postalCode") String postalCode,
                                 @RequestParam("tel") String tel,
                                 @RequestParam("lineID") String lineID,
                                 HttpSession httpSession) throws ParseException {
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
        if (lineID.equals("")) {
            lineID = null;
        }
        Customer customer = new Customer(email, pass, CustomerName, sex, date.parse(birthday), tel, country, city, region, street, postalCode, lineID);
        if (Objects.equals(httpSession.getAttribute("registerMail").toString(), email)) {//比對session中已驗證信箱與輸入之信箱
            customerService.insert(customer);
            mailService.sendMail(email, "已完成飯店註冊",
                    "您的帳號: " + email +
                            "\n用戶: " + CustomerName +
                            "\n註冊時間: " + new Date());
            httpSession.removeAttribute("registerMail");
            return "customer/customerLogin";
        } else {
            System.out.println("註冊失敗，信箱未經過驗證");
            return "customer/customerCreatAccount";
        }
    }

    //重設密碼-開始
    //1.轉忘記密碼
    @GetMapping("/customer/forget_password")
    public String forgetPassword() {
        return "customer/forgetPwd";
    }

    //2.寄送驗證碼
    @PostMapping("/customer/forget_password")
    @ResponseBody
    public String getMailCodeByHotel(@RequestParam("email") String email, HttpSession httpSession) {
        if (customerService.checkCustomerByEmail(email)) {//判斷信箱是否已註冊
            //寄送驗證碼
            mailService.sendResetPwdCodeMail(email, httpSession);
            httpSession.setAttribute("resetMail", email);
            return "Y";
        } else {
            return "N";
        }
    }

    //3.轉重設密碼頁面
    @GetMapping("/customer/reset_password")
    public String resetPassword(HttpSession httpSession) {
        //判斷是否已寄送mail
        if (httpSession.getAttribute("resetMail") != null) {
            return "customer/resetPwd";
        }
        return "redirect:/customer/forget_password";
    }

    //重設密碼-結束
    //4.密碼重設
    @PutMapping("/customer/reset_password")
    public String postResetPassword(@RequestParam("newPwd") String newPwd,
                                    @RequestParam("verificationCode") String verificationCode,
                                    HttpSession httpSession,
                                    Model model) {

        String resetMail = httpSession.getAttribute("resetMail").toString();
        String sessionCode = httpSession.getAttribute("verificationCode").toString();


        System.out.println("更新Pwd Email: " + resetMail);
        System.out.println("Session驗證碼: " + sessionCode);
        System.out.println("輸入驗證碼: " + verificationCode);

        if (Objects.equals(verificationCode, sessionCode)) {//比對驗證碼

            Boolean pwdResult = customerService.resetPwd(resetMail, newPwd);//更新密碼

            if (pwdResult) {
                httpSession.removeAttribute("resetMail");
                httpSession.removeAttribute("verificationCode");
                System.out.println(resetMail + "驗證碼已刪除");
                mailService.sendMail(resetMail, "密碼變更成功!!!", "您的帳號: " + resetMail + "\n密碼已變更" + "\n變更時間: " + new Date());//寄送密碼變更通知
                return "redirect:/customer/login";
            } else {
                model.addAttribute("errMsg", "驗證碼正確,密碼更新失敗");
                return "customer/resetpwd";
            }
        } else {
            model.addAttribute("errMsg", "驗證碼錯誤");
            return "customer/resetpwd";
        }
    }


    //line登入
    @GetMapping("/customer/lineLogin")
    public String lineLogin() {
        return "customer/lineLogin";
    }

    //line官方拿取token
    @PostMapping("/customer/getLineInformation")
    public ResponseEntity<?> handleLineLogin(@RequestParam("code") String code) {
        String tokenUrl = "https://api.line.me/oauth2/v2.1/token";

        // 構建交換授權碼的請求數據
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("code", code);
        params.add("redirect_uri", "http://localhost:8080/sip/customer/lineLogin");  // 一定要與 LINE Console 中設定的相同
        params.add("client_id", "2006237133");
        params.add("client_secret", "95cc8c87cac0f3ac4d9f24f94ada960f");

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

    //    Line登入判斷
    @PostMapping("/customer/createline")
    @ResponseBody
    public String createline(@RequestParam("aud") String lineID,
                             @RequestParam("email") String email,
                             HttpSession httpSession,
                             Model model) {
        System.out.println(lineID);
        System.out.println(email);
        Customer customer = customerService.findCustomerByEmail(email);
        Customer result = customerService.findLineID(lineID);
        if (result == null && customer == null) {
            httpSession.setAttribute("LineID", lineID);
            httpSession.setAttribute("registerMail", email);
            System.out.println("轉註冊頁");
            return "N";
        } else if (result == null && customer != null)//有email、無LineID 轉綁定Line頁
        {
            httpSession.setAttribute("lineID", lineID);
            httpSession.setAttribute("customerEmail", customer.getEmail());
            return "bindLineID";
        }
        httpSession.setAttribute("customerloginName", result.getCustomerName());
        httpSession.setAttribute("customerloginEmail", result.getEmail());
        httpSession.setAttribute("customerLoginId", result.getLoginID());
        httpSession.setAttribute("userLoggedIn", true);
        return "Y";
    }

    //驗證用戶_綁定LineID
    @GetMapping("customer/bindLine_checkPwd")
    public String bindLineID(HttpSession httpSession) {
        Boolean checkLineID = httpSession.getAttribute("lineID") != null;
        Boolean checkEmail = httpSession.getAttribute("customerEmail") != null;
        if (checkLineID && checkEmail) {
            return "customer/bindLineCheckPwd";
        } else {
            return "costomer/customerLogin";
        }
    }

    //修改LineID_綁定LineID
    @PutMapping("/customer/bindLineID")
    @ResponseBody
    public String checkCustomerLogin(@RequestParam("email") String email,
                                  @RequestParam("password") String password,
                                  @RequestParam("lineID") String lineID,
                                  HttpSession httpSession) {
        //檢查登入資訊_新增LineID
        Customer result = customerService.checkLogin(email, password);
        if (result != null) {
            result.setLineID(lineID);
            customerService.updateCustomer(result);
            //刪除全部Session。跳回登入頁 使用LINE登入
            httpSession.invalidate();
            return "Y";
        } else {
            System.out.println(email + " 登入失敗");
            return "N";
        }
    }


    //Google 登入
    @PostMapping("/customer/dashboard")
    @ResponseBody
    public String customerDashboardPage(@RequestBody Map<String, String> googleData, HttpSession httpSession) {

        if (googleData != null) {//判斷是否為Google登入
            String googleID = googleData.get("sub");
            String googleEmail = googleData.get("email");
//            String googleName = googleData.get("name");
            Customer result = customerService.oauth2CheckLogin(googleID);

            if (result != null) {// 有帳號=>後台頁
                httpSession.setAttribute("customerLoginId", result.getLoginID());
                httpSession.setAttribute("customerloginEmail", result.getEmail());
                httpSession.setAttribute("customerloginName", result.getCustomerName());
                System.out.println("GoogleLogin: " + googleEmail + " GoogleID: " + googleID);
                System.out.println(result.getLoginID() + "-" + result.getCustomerName() + " Login Success !!");
                return "Y";
            } else {// 未有帳號=>註冊頁
                System.out.println("GoogleLogin Fail!!" + " GoogleID: " + googleID);
//                model.addAttribute("errMsg", "此Google帳號尚未綁定<br>請先註冊或登入綁定");
                return "N";
            }
        }
        return "N";//都不是=>登入頁
    }


}
