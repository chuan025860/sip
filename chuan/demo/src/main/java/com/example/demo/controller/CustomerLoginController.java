package com.example.demo.controller;



import com.example.demo.bean.Customer;
import com.example.demo.security.JwtTokenProvider;
import com.example.demo.service.CustomerService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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

import java.util.HashMap;
import java.util.Map;


@Controller
public class CustomerLoginController {
    @Autowired
    CustomerService customerService;
    @Autowired
    JwtTokenProvider jwtTokenProvider;


    //登入頁
    @GetMapping("/customer/login")
    @ResponseBody
    public ResponseEntity<String> customerLoginPage() {
        return ResponseEntity.ok().body("success");
    }

    // 登入判斷
    @PostMapping("/customer/login")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkCustomerLogin(@RequestBody Customer customer) {
        Map<String, Object> response = new HashMap<>();
         customer = customerService.checkLogin(customer.getEmail(), customer.getPassword());
        if (customer != null) {
            String token = jwtTokenProvider.generateToken(customer.getLoginID(), customer.getCustomerName());
            response.put("code", 200);
            response.put("message", "登入成功");
            Map<String, Object> data = new HashMap<>();
            data.put("customer", customer);
            data.put("token", token);
            response.put("data", data);
        } else {
            response.put("code", 401);
            response.put("message", "email或密碼錯誤");
            response.put("data", null);
        }
        return ResponseEntity.ok(response);
    }



    //登出(刪除全部Session)
    @GetMapping("/customer/logout")
    public String customerLogout(HttpSession httpSession, HttpServletResponse response) {
        if (httpSession != null) {
            httpSession.invalidate();
            // 刪除 JWT Cookie
            Cookie jwtCookie = new Cookie("Authorization", null);
            jwtCookie.setPath("/"); // 設定 cookie 路徑，確保它能在整個應用中被刪除
            jwtCookie.setMaxAge(0); // 設定 cookie 的最大生存時間為 0，表示立即過期
            response.addCookie(jwtCookie); // 添加 cookie 到響應中
            return "redirect:/customer/login";
        }
        return "customer/customerlogin";
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

    // Line登入判斷
    @PostMapping("/customer/createline")
    @ResponseBody
    public Map<String, String> createline(@RequestParam("aud") String lineID,
                                          @RequestParam("email") String email,
                                          HttpSession httpSession,
                                          Model model) {
        System.out.println(lineID);
        System.out.println(email);
        Customer customer = customerService.findCustomerByEmail(email);
        Customer result = customerService.findLineID(lineID);
        Map<String, String> responseMap = new HashMap<>();
        if (result == null && customer == null) {
            httpSession.setAttribute("LineID", lineID);
            httpSession.setAttribute("registerMail", email);
            System.out.println("轉註冊頁");
            responseMap.put("status", "N");
            return responseMap;
        } else if (result == null && customer != null)//有email、無LineID 轉綁定Line頁
        {
            httpSession.setAttribute("lineID", lineID);
            httpSession.setAttribute("customerEmail", customer.getEmail());
            responseMap.put("status", "bindLineID");
            return responseMap;
        }
        httpSession.setAttribute("customerloginEmail", result.getEmail());
        String token = jwtTokenProvider.generateToken(customer.getLoginID(), customer.getCustomerName());
        System.out.println(token);
        responseMap.put("status", "Y");
        responseMap.put("token", token);
        return responseMap;
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
                                     HttpSession httpSession,
                                     HttpServletResponse response) {
        //檢查登入資訊_新增LineID
        Customer result = customerService.checkLogin(email, password);
        if (result != null) {
            result.setLineID(lineID);
            customerService.updateCustomer(result);
            //刪除全部Session。跳回登入頁 使用LINE登入
            httpSession.invalidate();
            // 刪除 JWT Cookie  跳回登入頁
            Cookie jwtCookie = new Cookie("Authorization", null);
            jwtCookie.setPath("/"); // 設定 cookie 路徑，確保它能在整個應用中被刪除
            jwtCookie.setMaxAge(0); // 設定 cookie 的最大生存時間為 0，表示立即過期
            response.addCookie(jwtCookie); // 添加 cookie 到響應中
            return "Y";
        } else {
            System.out.println(email + " 登入失敗");
            return "N";
        }
    }


    //Google 登入
    @PostMapping("/customer/dashboard")
    @ResponseBody
    public Map<String, String> customerDashboardPage(@RequestBody Map<String, String> googleData, HttpSession httpSession) {
        Map<String, String> response = new HashMap<>();
        if (googleData != null) {//判斷是否為Google登入
            String googleID = googleData.get("sub");
            String googleEmail = googleData.get("email");
//            String googleName = googleData.get("name");
            Customer result = customerService.oauth2CheckLogin(googleID);

            if (result != null) {// 有帳號=>後台頁
                httpSession.setAttribute("customerloginEmail", result.getEmail());
                System.out.println("GoogleLogin: " + googleEmail + " GoogleID: " + googleID);
                System.out.println(result.getLoginID() + "-" + result.getCustomerName() + " Login Success !!");
                String token = jwtTokenProvider.generateToken(result.getLoginID(), result.getCustomerName());
                response.put("status", "Y");
                response.put("token", token);
                return response;
            } else {// 未有帳號=>註冊頁
                System.out.println("GoogleLogin Fail!!" + " GoogleID: " + googleID);
//                model.addAttribute("errMsg", "此Google帳號尚未綁定<br>請先註冊或登入綁定");
                response.put("status", "Y");
                return response;
            }
        }
        response.put("status", "Y");
        return response;//都不是=>登入頁
    }


}
