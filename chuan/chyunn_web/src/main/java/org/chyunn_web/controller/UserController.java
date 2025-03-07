package org.chyunn_web.controller;

import jakarta.servlet.http.HttpSession;
import org.chyunn_web.bean.User;
import org.chyunn_web.security.JwtTokenProvider;
import org.chyunn_web.service.JwtBlacklistService;
import org.chyunn_web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class UserController {

    @Autowired
    UserService userService;
    @Autowired
    private PasswordEncoder pwdEncoder;
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    JwtBlacklistService jwtBlacklistService;

    //登入頁
    @GetMapping("/login")
    public String LoginPage(@RequestParam(name = "USERID", required = false) String userId,
                            @RequestParam(name = "LOGINID", required = false) String loginId,
                            @RequestParam(name = "USERNAME", required = false) String username
    ) {
        return "incident/userLogin";
    }

    // 登入判斷
    @PostMapping("/user/login")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkLogin(@RequestParam("id") String id,
                                                          @RequestParam("password") String inputPwd,
                                                          HttpSession httpSession) {
        Map<String, Object> response = new HashMap<>();
        //檢查登入資訊
        User dbUser = userService.getTestUser(id);

        if (pwdEncoder.matches(inputPwd, dbUser.getPassword())) {
//            httpSession.setAttribute("loginID", dbUser.getId());
            String token = jwtTokenProvider.generateToken(dbUser.getLoginId(), dbUser.getUsername());
            response.put("code", 200);
            response.put("message", "success");
            response.put("userName", dbUser.getUsername());
            response.put("token", token);
            return ResponseEntity.ok(response);
        } else {
            response.put("code", 401);
            response.put("message", "資料錯誤");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

    }

    @PostMapping("/user/register")
    @ResponseBody
    public String postlogin(@RequestParam("id") String id,
                            @RequestParam("password") String password
    ) {
        String encodedPwd = pwdEncoder.encode(password);
        User testUser = new User();
        testUser.setLoginId(id);
        testUser.setUsername("沈世泉");
        testUser.setUserId(id);
        testUser.setPassword(encodedPwd);
        userService.saveUser(testUser);
        return "success";
    }


    @PostMapping("/user/logout")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> logout(@RequestHeader("Authorization") String token, HttpSession httpSession) {
        Map<String, Object> response = new HashMap<>();

        try {

            // 確認 token 是否有效
            if (jwtTokenProvider.validateToken(token)) {
                jwtBlacklistService.addToBlacklist(token);
                // 清除 session
                httpSession.invalidate();

                response.put("code", 200);
                response.put("message", "登出成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 401);
                response.put("message", "無效的 token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            // 異常處理
            response.put("code", 500);
            response.put("message", "登出處理錯誤");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("user/easyRegister")
    @ResponseBody
    public Map<String, Object> easyRegister(@RequestParam(name = "USERID", required = false) String userId,
                                            @RequestParam(name = "LOGINID", required = false) String loginId,
                                            @RequestParam(name = "USERNAME", required = false) String userName
    ) {
        Map<String, Object> response = new HashMap<>();
        // 如果有 USERID，則存入資料庫
        if (userId != null && loginId != null && userName != null) {
            String token;
            // 先檢查是否已經存在該用戶
            if (!userService.existsByUserId(userId)) {
                User user = new User();
                user.setUserId(userId);
                user.setLoginId(loginId);
                user.setUsername(userName);
                String encodedPwd = pwdEncoder.encode("default");
                user.setPassword(encodedPwd); // 加密存儲密碼
                try {
                    userService.saveUser(user);
                    token = jwtTokenProvider.generateToken(loginId, userName); // 產生 Token
                    response.put("status", "success");
                    response.put("message", "使用者註冊成功");
                    response.put("token", token); // 回傳 Token
                    response.put("userName", userName);
                } catch (Exception e) {
                    e.printStackTrace();
                    response.put("status", "error");
                    response.put("message", "儲存使用者失敗");
                    return response;
                }
            } else {
                // 使用者已存在，直接回傳 Token
                User dbUser = userService.getTestUser(loginId);
                token = jwtTokenProvider.generateToken(dbUser.getLoginId(), dbUser.getUsername());
                response.put("status", "success");
                response.put("message", "使用者已存在，直接登入");
                response.put("token", token);
                response.put("userName", userName);
            }
        } else {
            System.out.println("test");
            response.put("status", "error");
            response.put("message", "缺少必要參數");
        }
        return response;
    }
}
