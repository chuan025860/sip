package org.chyunn_web.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.chyunn_web.Request.CreateUserRequest;
import org.chyunn_web.bean.User.User;
import org.chyunn_web.bean.User.UserRole;
import org.chyunn_web.bean.User.UserRoleId;
import org.chyunn_web.bean.admin.Department;
import org.chyunn_web.bean.admin.Position;
import org.chyunn_web.bean.admin.SubSubUnit;
import org.chyunn_web.bean.admin.SubUnit;
import org.chyunn_web.security.JwtTokenProvider;
import org.chyunn_web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Controller
public class indexController {
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    UserService userService;
    @Autowired
    PasswordEncoder pwdEncoder;

    @GetMapping("/index/index")
    public String index(
    ) {
        return "index/index";
    }

    @GetMapping("/index/accountSettings")
    public String accountSettings(HttpServletRequest request, Model model
    ) {
        String token = null;
        // 從 Cookie 中取得 token
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("authToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        String loginId = jwtTokenProvider.getLoginIdFromToken(token);
        User user = userService.getUser(loginId);
        List<Department> tree = userService.findAllWithTree();
        List<String> userPerms = user.getRoles().stream()
                .map(r -> r.getId().getRole())   // 假設你的 UserRole 裡有 getId().getRole()
                .collect(Collectors.toList());
        model.addAttribute("userPerms", userPerms);
        model.addAttribute("orgTree", tree);
        model.addAttribute("user", user);
        return "index/accountSettings";
    }

    @PostMapping("/index/accountSettings_post")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> update_user_post(@RequestBody CreateUserRequest req, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        String loginId = req.getLoginId();
        String token = null;
        // 從 Cookie 中取得 token
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("authToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        String userLoginId = jwtTokenProvider.getLoginIdFromToken(token);
        User user = userService.getUser(loginId);
        if (!loginId.equals(userLoginId)) {
            response.put("message", "無權修改其他使用者帳號資料。");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        if (user == null) {
            response.put("message", "找不到使用者 " + req.getOriginalLoginId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        if (StringUtils.hasText(req.getPassword())) {
            user.setPassword(pwdEncoder.encode(req.getPassword()));
        }
        userService.saveUser(user);
        return ResponseEntity.ok(response);
    }

    @ModelAttribute
    public void addUserInfoToModel(HttpServletRequest request, Model model) {
        String token = null;

        // 從 Cookie 中取得 token
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
            String userName = jwtTokenProvider.getUserNameFromToken(token);
            List<String> roles = jwtTokenProvider.getUserRolesFromToken(token);

            model.addAttribute("userName", userName);
            model.addAttribute("userRoles", roles);
            model.addAttribute("creator", userName);
        }
    }
}
