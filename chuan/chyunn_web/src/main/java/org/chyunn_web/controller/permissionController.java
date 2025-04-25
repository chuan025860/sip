package org.chyunn_web.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.chyunn_web.bean.User;
import org.chyunn_web.exception.UserNotFoundException;
import org.chyunn_web.security.JwtTokenProvider;
import org.chyunn_web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class permissionController {
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    UserService userService;


    @GetMapping("/permission/permission_admin")
    public String permission_admin() {
        return "permission/permission_admin";
    }

    @PostMapping("/permission/selectuser")
    public ResponseEntity<Map<String, Object>> permission_selectuser(@RequestParam("loginId") String loginId) {
        User user=userService.getUser(loginId);
        if (user == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "使用者不存在");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        List<String> permissions = user.getRoles().stream()
                .map(userRole -> userRole.getId().getRole()) // 從複合主鍵取得角色名稱
                .collect(Collectors.toList());
        System.out.println(permissions.get(0));
        Map<String, Object> response = new HashMap<>();
        response.put("permissions", permissions);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/permission/updateuser")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateUserPermissions(
            @RequestParam String loginId,
            @RequestParam List<String> permissions,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();
        try {
            // 從 Cookie 取得 token
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
                // 查找被修改者的角色
                List<String> targetRoles = userService.findRolesByLoginId(loginId);
                if (targetRoles.contains("ROLE_ADMIN")) {
                    response.put("code", 400);
                    response.put("message", "該使用者已是最高管理者，無法修改權限");
                    return ResponseEntity.badRequest().body(response); // 400 錯誤，已是管理員
                }

                // 執行更新
                userService.updateUserPermissions(loginId, permissions);
                response.put("code", 200);
                response.put("message", "使用者權限已更新");
                return ResponseEntity.ok(response); // 200 成功

            }

            response.put("code", 401);
            response.put("message", "無效的登入資訊");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response); // 401 無效登入

        } catch (UserNotFoundException e) {
            // 使用者找不到錯誤
            response.put("code", 404);
            response.put("message", e.getMessage()); // 可以將自定義的訊息傳遞
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response); // 404 使用者不存在

        } catch (Exception e) {
            e.printStackTrace();
            response.put("code", 500);
            response.put("message", "系統錯誤，請稍後再試");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // 500 系統錯誤
        }
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
