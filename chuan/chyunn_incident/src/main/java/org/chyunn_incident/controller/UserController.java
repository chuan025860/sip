package org.chyunn_incident.controller;

import jakarta.servlet.http.HttpSession;
import org.chyunn_incident.bean.TestUser;
import org.chyunn_incident.repository.TestUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
public class UserController {
    @Autowired
    TestUserRepository testUserRepository;
    @Autowired
    private PasswordEncoder pwdEncoder;


    //登入頁
    @GetMapping("/login")
    public String LoginPage(HttpSession httpSession) {

        //判斷是否已經登入
        if (httpSession.getAttribute("loginID") != null) {
            return "redirect:/incident/select_incident";
        }
        return "incident/userLogin";
    }

    // 登入判斷
    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkLogin(@RequestParam("id") String id,
                                                               @RequestParam("password") String inputPwd,
                                                               HttpSession httpSession) {
        Map<String, Object> response = new HashMap<>();
        //檢查登入資訊
        Optional<TestUser> optional = testUserRepository.findById(id);
        if (optional.isPresent()) {
            TestUser dbUser = optional.get();
            if (pwdEncoder.matches(inputPwd, dbUser.getPassword())) {
                httpSession.setAttribute("loginID", dbUser.getId());
                response.put("code", 200);
                response.put("message", "success");
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 401);
                response.put("message", "資料錯誤");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

        } else {
            response.put("code", 401);
            response.put("message", "資料錯誤");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/register")
    @ResponseBody
    public String postlogin(@RequestParam("id") String id,
                                 @RequestParam("password") String password
    ) {
        String encodedPwd = pwdEncoder.encode(password);
        TestUser testUser = new TestUser();
        testUser.setId(id);
        testUser.setPassword(encodedPwd);
        testUserRepository.save(testUser);
        return "success";
    }


    //登出
    @GetMapping("/logout")
    public String Logout(HttpSession httpSession) {
        if (httpSession != null) {
            httpSession.invalidate();
            return "redirect:/login";
        }
        return "incident/userLogin";
    }
}
