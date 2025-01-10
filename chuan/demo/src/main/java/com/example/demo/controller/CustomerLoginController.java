package com.example.demo.controller;


import com.example.demo.bean.ChatRoom;
import com.example.demo.bean.Customer;
import com.example.demo.security.JwtTokenProvider;
import com.example.demo.service.ChatService;
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

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Controller
public class CustomerLoginController {
    @Autowired
    CustomerService customerService;
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    ChatService chatService;


    //登入頁
    @GetMapping("/customer/login")
    @ResponseBody
    public ResponseEntity<String> customerLoginPage() {
        return ResponseEntity.ok().body("success");
    }

    // 註冊
    @PostMapping("/customer/register")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> registerCustomer(@RequestBody Customer customer) {
        Map<String, Object> response = new HashMap<>();
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");

        if (customerService.insert(customer)) {
            response.put("code", 200);
            response.put("message", "註冊成功");
            Map<String, Object> data = new HashMap<>();
            data.put("customer", customer);
            response.put("data", data);
        } else {
            response.put("code", 401);
            response.put("message", "資料錯誤");
            response.put("data", null);
        }
        return ResponseEntity.ok(response);
    }


    // 登入判斷
    @PostMapping("/customer/login")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkCustomerLogin(@RequestBody Customer customer) {
        Map<String, Object> response = new HashMap<>();
        customer = customerService.checkLogin(customer.getPhone(), customer.getPassword());
        ChatRoom chatRoom = new ChatRoom();
//        chatRoom.setRoomID(UUID.randomUUID());
//        chatRoom.setUser1("dimimg92306@gmail.com");
//        chatRoom.setUser2("test@gmail.com");
//        chatService.saveChatRoom(chatRoom);
//        chatService.getChatRoom(UUID.fromString("chatroom:dba9f147-e45e-49d2-b6ef-b472945a28be"));
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

    // 登入判斷
    @PostMapping("/customer/index/select")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> selectCustomer(@RequestBody Customer customer) {
        Map<String, Object> response = new HashMap<>();
        customer = customerService.checkLogin(customer.getPhone(), customer.getPassword());
        if (customer != null) {
//            String token = jwtTokenProvider.generateToken(customer.getLoginID(), customer.getCustomerName());
            response.put("code", 200);
            response.put("message", "進入首頁");
            Map<String, Object> data = new HashMap<>();
            data.put("customer", customer);
//            data.put("token", token);
            response.put("data", data);
        } else {
            response.put("code", 401);
            response.put("message", "email或密碼錯誤");
            response.put("data", null);
        }
        return ResponseEntity.ok(response);
    }

    //錯誤Token
    @GetMapping("/customer/errorToken")
    @ResponseBody
    public ResponseEntity<String> errorToken() {
        return ResponseEntity.ok().body("errorToken");
    }

    //無Token
    @GetMapping("/customer/needToken")
    @ResponseBody
    public ResponseEntity<String> needToken() {
        return ResponseEntity.ok().body("needToken");
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


}
