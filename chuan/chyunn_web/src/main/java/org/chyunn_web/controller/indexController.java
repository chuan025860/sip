package org.chyunn_web.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.chyunn_web.Request.CreateUserRequest;
import org.chyunn_web.bean.Resource.ResourceBorrowRequest;
import org.chyunn_web.bean.User.User;
import org.chyunn_web.bean.User.UserRole;
import org.chyunn_web.bean.User.UserRoleId;
import org.chyunn_web.bean.admin.Department;
import org.chyunn_web.bean.admin.Position;
import org.chyunn_web.bean.admin.SubSubUnit;
import org.chyunn_web.bean.admin.SubUnit;
import org.chyunn_web.bean.incident.Incident;
import org.chyunn_web.dto.EventDTO;
import org.chyunn_web.dto.IncidentDto;
import org.chyunn_web.security.JwtTokenProvider;
import org.chyunn_web.service.IncidentService;
import org.chyunn_web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class indexController {
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    UserService userService;
    @Autowired
    PasswordEncoder pwdEncoder;
    @Autowired
    IncidentService incidentService;

    @GetMapping("/index/index")
    public String index(Model model
    ) {
        List<Incident> incidents = incidentService.findTop5VisibleNotDiscardedOrderByLastReportTimeDesc();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        List<IncidentDto> incidentDtos = new ArrayList<>();
        for (Incident incident : incidents) {
            IncidentDto incidentDto = incidentService.convertIncidentToIncidentDto(incident);
            incidentDto.setReportDate(simpleDateFormat.format(incident.getReportDate()));
            incidentDto.setHandler(userService.getUser(incident.getHandler()).getUsername());
            incidentDto.setLastReportTime(incident.getLastReportTime().format(dateTimeFormatter));
            incidentDtos.add(incidentDto);
        }
        model.addAttribute("incidents", incidentDtos);
        return "index/index_test";
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
        // 驗證 email 是否為空
        if (!StringUtils.hasText(req.getEmail())) {
            response.put("message", "Email 不可為空白。");
            return ResponseEntity.badRequest().body(response);
        }

        // 可選擇進一步驗證 email 格式（簡單檢查）
        if (!req.getEmail().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            response.put("message", "Email 格式不正確。");
            return ResponseEntity.badRequest().body(response);
        }

        // 更新 email
        user.setEmail(req.getEmail());

        // 更新密碼
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
            model.addAttribute("uri", request.getRequestURI());
            model.addAttribute("userName", userName);
            model.addAttribute("userRoles", roles);
            model.addAttribute("creator", userName);
        }
    }
}
