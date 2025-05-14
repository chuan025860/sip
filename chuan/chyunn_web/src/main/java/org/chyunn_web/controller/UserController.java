package org.chyunn_web.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.chyunn_web.bean.User;
import org.chyunn_web.bean.UserRole;
import org.chyunn_web.bean.UserRoleId;
import org.chyunn_web.security.JwtTokenProvider;
import org.chyunn_web.service.JwtBlacklistService;
import org.chyunn_web.service.MailService;
import org.chyunn_web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
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
    @Autowired
    MailService mailService;

    //登入頁
    @GetMapping("/login")
    public String LoginPage(@RequestParam(name = "USERID", required = false) String userId,
                            @RequestParam(name = "LOGINID", required = false) String loginId,
                            @RequestParam(name = "USERNAME", required = false) String username
    ) {
//        mailService.sendSimpleMail("test","test2");
//        System.out.println("aaaa");
        return "chyunnLogin/userLogin";
    }
    //登入頁
    @GetMapping("/loginMobile")
    public String LoginPage_mobile(@RequestParam(name = "USERID", required = false) String userId,
                            @RequestParam(name = "LOGINID", required = false) String loginId,
                            @RequestParam(name = "USERNAME", required = false) String username
    ) {
        return "chyunnLogin/userLogin_mobile";
    }

    // 登入判斷
    @PostMapping("/user/login")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkLogin(@RequestParam("id") String id,
                                                          @RequestParam("password") String inputPwd,
                                                          HttpServletResponse response) {
        Map<String, Object> responseBody = new HashMap<>();
        //檢查登入資訊
        User dbUser = userService.getUser(id);

        if (pwdEncoder.matches(inputPwd, dbUser.getPassword())) {
            String token = jwtTokenProvider.generateToken(dbUser.getLoginId(), dbUser.getUsername(),dbUser.getRoles());
            // 設定 HttpOnly Cookie
            ResponseCookie cookie = ResponseCookie.from("authToken", token)
                    .httpOnly(true)         // JS 無法存取
                    .secure(false)          // 若有 https 請設成 true
                    .path("/")              // 全站有效
                    .maxAge(1800000)     //  30 分鐘
                    .sameSite("Lax")        // 可依需要調整
                    .build();

            response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            responseBody.put("code", 200);
            responseBody.put("userName", dbUser.getUsername());
            return ResponseEntity.ok(responseBody);
        } else {
            responseBody.put("code", 401);
            responseBody.put("message", "資料錯誤");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
        }

    }

//    @PostMapping("/user/register")
//    @ResponseBody
//    public String postlogin(@RequestParam("id") String id,
//                            @RequestParam("password") String password
//    ) {
//        String encodedPwd = pwdEncoder.encode(password);
//        User testUser = new User();
//        testUser.setLoginId(id);
//        testUser.setUsername("測試1號");
//        testUser.setUserId(id);
//        testUser.setPassword(encodedPwd);
//        UserRole defaultRole = new UserRole();
//        defaultRole.setUser(testUser);
//
//        // 建立角色
//        List<UserRole> roles = new ArrayList<>();
//        UserRole role1 = new UserRole();
//        role1.setId(new UserRoleId(id, "ROLE_USER"));
//        role1.setUser(testUser);
//        roles.add(role1);
//
//        testUser.setRoles(roles); // 設定進 user 的角色關係
//        userService.saveUser(testUser);
//        return "success";
//    }


    @PostMapping("/user/logout")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request, HttpSession httpSession, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();


        try {
            // 從 Cookie 中取得 token
            String token = null;
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("authToken".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }

            if (token != null && jwtTokenProvider.validateToken(token)) {
                // 加入黑名單
                jwtBlacklistService.addToBlacklist(token);

                // 清除 session
                httpSession.invalidate();

                // 清除 authToken Cookie
                Cookie cookie = new Cookie("authToken", null);
                cookie.setHttpOnly(true);
                cookie.setPath("/"); // 確保路徑正確
                cookie.setMaxAge(0); // 設定過期
                response.addCookie(cookie);

                result.put("code", 200);
                result.put("message", "登出成功");
                return ResponseEntity.ok(result);
            } else {
                result.put("code", 401);
                result.put("message", "無效的 token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
            }

        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "登出處理錯誤");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    @PostMapping("user/easyRegister")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> easyRegister(@RequestParam(name = "USERID", required = false) String userId,
                                            @RequestParam(name = "LOGINID", required = false) String loginId,
                                            @RequestParam(name = "USERNAME", required = false) String userName,
                                            HttpServletResponse response
    ) {
        Map<String, Object> responseBody = new HashMap<>();
        // 如果有 USERID，則存入資料庫
        if (userId != null && loginId != null && userName != null) {
            String token;
            // 先檢查是否已經存在該用戶
            if (!userService.existsByUserId(userId)) {
                User user = new User();
                user.setUserId(userId);
                user.setLoginId(loginId);
                user.setUsername(userName);
                String encodedPwd = pwdEncoder.encode("chyunn_web");
                user.setPassword(encodedPwd); // 加密存儲密碼
                UserRole defaultRole = new UserRole();
                defaultRole.setUser(user);

                // 建立角色
                List<UserRole> roles = new ArrayList<>();
                UserRole role1 = new UserRole();
                role1.setId(new UserRoleId(loginId, "ROLE_USER"));
                role1.setUser(user);
                roles.add(role1);

                user.setRoles(roles); // 設定進 user 的角色關係
                try {
                    userService.saveUser(user);
                    token = jwtTokenProvider.generateToken(loginId, userName,user.getRoles()); // 產生 Token
                    // 設定 HttpOnly Cookie
                    ResponseCookie cookie = ResponseCookie.from("authToken", token)
                            .httpOnly(true)         // JS 無法存取
                            .secure(false)          // 若有 https 請設成 true
                            .path("/")              // 全站有效
                            .maxAge(1800000)     //  30 分鐘
                            .sameSite("Lax")        // 可依需要調整
                            .build();

                    response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
                    responseBody.put("code", 200);
                    responseBody.put("userName", userName);
                    return ResponseEntity.ok(responseBody);
                } catch (Exception e) {
                    e.printStackTrace();
                    responseBody.put("status", "error");
                    responseBody.put("message", "儲存使用者失敗");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
                }
            } else {
                // 使用者已存在，直接回傳 Token
                User dbUser = userService.getUser(loginId);
                token = jwtTokenProvider.generateToken(dbUser.getLoginId(), dbUser.getUsername(),dbUser.getRoles());
                // 設定 HttpOnly Cookie
                ResponseCookie cookie = ResponseCookie.from("authToken", token)
                        .httpOnly(true)         // JS 無法存取
                        .secure(false)          // 若有 https 請設成 true
                        .path("/")              // 全站有效
                        .maxAge(1800000)     //  30 分鐘
                        .sameSite("Lax")        // 可依需要調整
                        .build();

                response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
                responseBody.put("userName", userName);
                return ResponseEntity.ok(responseBody);
            }
        } else {
            System.out.println("test");
            responseBody.put("status", "error");
            responseBody.put("message", "缺少必要參數");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
        }
    }
}
