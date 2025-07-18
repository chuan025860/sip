package org.chyunn_web.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.chyunn_web.Request.CreateUserRequest;
import org.chyunn_web.bean.User.UserRole;
import org.chyunn_web.bean.User.UserRoleId;
import org.chyunn_web.bean.admin.Department;
import org.chyunn_web.bean.User.User;
import org.chyunn_web.bean.admin.Position;
import org.chyunn_web.bean.admin.SubSubUnit;
import org.chyunn_web.bean.admin.SubUnit;
import org.chyunn_web.dto.UserDto;
import org.chyunn_web.exception.UserNotFoundException;
import org.chyunn_web.security.JwtTokenProvider;
import org.chyunn_web.service.DepartmentService;
import org.chyunn_web.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class permissionController {
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    UserService userService;
    @Autowired
    PasswordEncoder pwdEncoder;
    @Autowired
    DepartmentService departmentService;

    private final Logger logger = LoggerFactory.getLogger(permissionController.class);

    @GetMapping("/permission/permission_admin")
    public String permission_admin() {
        return "permission/permission_admin";
    }

    @GetMapping("/permission/create_user")
    public String create_user(Model model) {
        List<Department> tree = userService.findAllWithTree();
        model.addAttribute("orgTree", tree);
        return "permission/create_user";
    }

    @GetMapping("/permission/update_user")
    public String update_user(@RequestParam String loginId, Model model) {
        User user = userService.getUser(loginId);
        List<Department> tree = userService.findAllWithTree();
        List<String> userPerms = user.getRoles().stream()
                .map(r -> r.getId().getRole())   // 假設你的 UserRole 裡有 getId().getRole()
                .collect(Collectors.toList());
        model.addAttribute("userPerms", userPerms);
        model.addAttribute("orgTree", tree);
        model.addAttribute("user", user);
        return "permission/update_user";
    }

    @GetMapping("/permission/manage_user")
    public String manage_user() {
        return "permission/manage_user";
    }

    @PostMapping("/permission/manage_user_findAll")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> manage_user_findAll(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page, // 頁數
            @RequestParam(defaultValue = "50") int size // 每頁顯示多少筆資料
    ) {
        // 1. 建立分頁物件
        Pageable pageable = PageRequest.of(page, size);

        // 2. 用分頁方法取回 Page<User>
        Page<User> userPage = userService.findAllWithSpecialOrder(pageable);

        // 3. 將 User 轉成 DTO
        List<UserDto> userDtoList = new ArrayList<>();
        for (User user : userPage.getContent()) {
            UserDto userDto = new UserDto();
            userDto.setLoginId(user.getLoginId());
            userDto.setUsername(user.getUsername());
            userDto.setEmail(user.getEmail());
            userDto.setEnabled(user.isEnabled());
            if (user.getDepartment() == null) {
                userDto.setDepartmentName("");
            } else {
                userDto.setDepartmentName(user.getDepartment().getName());
            }
            userDtoList.add(userDto);
        }

        // 4. 準備回傳結構
        Map<String, Object> response = new HashMap<>();
        response.put("content", userDtoList);
        response.put("number", userPage.getNumber());
        response.put("size", userPage.getSize());
        response.put("totalPages", userPage.getTotalPages());
        response.put("totalElements", userPage.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/permission/manage_user_select")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> manage_user_select(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page, // 頁數
            @RequestParam(defaultValue = "50") int size // 每頁顯示多少筆資料
    ) {
        // 1. 建立分頁物件
        Pageable pageable = PageRequest.of(page, size);
        System.out.println(search);
        // 2. 用分頁方法取回 Page<User>
       Page<User> userPage = userService.findUserKeyword(search,pageable);

        // 3. 將 User 轉成 DTO
        List<UserDto> userDtoList = new ArrayList<>();
        for (User user : userPage.getContent()) {
            UserDto userDto = new UserDto();
            userDto.setLoginId(user.getLoginId());
            userDto.setUsername(user.getUsername());
            userDto.setEmail(user.getEmail());
            userDto.setEnabled(user.isEnabled());
            if (user.getDepartment() == null) {
                userDto.setDepartmentName("");
            } else {
                userDto.setDepartmentName(user.getDepartment().getName());
            }
            userDtoList.add(userDto);
        }
        System.out.println(userDtoList.size());

        // 4. 準備回傳結構
        Map<String, Object> response = new HashMap<>();
        response.put("content", userDtoList);
        response.put("number", userPage.getNumber());
        response.put("size", userPage.getSize());
        response.put("totalPages", userPage.getTotalPages());
        response.put("totalElements", userPage.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/permission/create_user")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> create_user_post(@RequestBody CreateUserRequest req) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 1. 檢查是否已存在
            if (userService.existsById(req.loginId)) {
                response.put("message", "帳號已存在");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            // 2. 建立新的 User 物件並填值
            User user = new User();
            user.setLoginId(req.getLoginId());
            user.setUsername(req.getUsername());
            user.setEmployee_no(req.getEmployeeNo());
            user.setEmail(req.getEmail());
            user.setPassword(pwdEncoder.encode(req.getPassword()));

            // 3. Department （必填）
            try {
                Department dept = departmentService.findByIdByDepartment(req.getDepartmentId());
                user.setDepartment(dept);
            } catch (NoSuchElementException ex) {
                throw new IllegalArgumentException("找不到部門 ID=" + req.getDepartmentId());
            }

            // 4. SubUnit （選填）
            if (req.getSubUnitId() != null) {
                try {
                    SubUnit su = departmentService.findByIdBySubUnit(req.getSubUnitId());
                    user.setSubUnit(su);
                } catch (NoSuchElementException ex) {
                    throw new IllegalArgumentException("找不到子單位 ID=" + req.getSubUnitId());
                }
            }

            // 5. SubSubUnit （選填）
            if (req.getSubSubUnitId() != null) {
                try {
                    SubSubUnit ssu = departmentService.findByIdBySubSubUnit(req.getSubSubUnitId());
                    user.setSubSubUnit(ssu);
                } catch (NoSuchElementException ex) {
                    throw new IllegalArgumentException("找不到子子單位 ID=" + req.getSubSubUnitId());
                }
            }

            // 6. Position （必填）
            try {
                Position pos = departmentService.findByIdByPosition(req.getPositionId());
                user.setPosition(pos);
            } catch (NoSuchElementException ex) {
                throw new IllegalArgumentException("找不到職位 ID=" + req.getPositionId());
            }

            // 7.設定權限
            if (!req.getPermissions().isEmpty()) {
                List<UserRole> roles = new ArrayList<>();
                for (String role : req.getPermissions()) {
                    UserRole userRole = new UserRole();
                    userRole.setUser(user);
                    userRole.setId(new UserRoleId(user.getId(), role));
                    roles.add(userRole);
                }
                user.setRoles(roles);
            }

            // 8. 存檔
            userService.saveUser(user);

            // 成功回應
            response.put("message", "建立完成");
            return ResponseEntity.ok(response);

        } catch (DataIntegrityViolationException dive) {
            // 資料庫約束（如欄位長度、唯一鍵）違規
            logger.error("資料儲存失敗，違反完整性約束", dive);
            response.put("message", "資料格式有誤或重複，儲存失敗");
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        } catch (Exception ex) {
            // 其他未預期例外
            logger.error("建立使用者時發生例外", ex);
            response.put("message", "系統錯誤，請稍後再試");
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }


    @PostMapping("/permission/update_user")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> update_user_post(@RequestBody CreateUserRequest req,HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        String newLoginId = req.getLoginId();
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
        String currentLoginId  = jwtTokenProvider.getLoginIdFromToken(token);
        User currentUser = userService.getUser(currentLoginId);
        boolean isAdmin = currentUser.getRoles().stream()
                .map(r -> r.getId().getRole())
                .anyMatch(role -> "ROLE_ADMIN".equals(role));
        // 若要編輯的是 BA000，但登入者不是 ROLE_ADMIN，就禁止
        if ("BA000".equals(req.getOriginalLoginId()) && !isAdmin) {
            response.put("message", "您沒有權限修改 BA000 的帳號資料");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        User user = userService.getUser(req.getOriginalLoginId());
        if (user == null) {
            response.put("message", "找不到使用者 " + req.getOriginalLoginId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        for (UserRole ur : user.getRoles()) {
            if ("ROLE_ADMIN".equals(ur.getId().getRole())) {
                if (!newLoginId.equals(req.getOriginalLoginId())) {
                    user.setLoginId(newLoginId);
                }
                user.setUsername(req.getUsername());
                user.setEmployee_no(req.getEmployeeNo());
                user.setEmail(req.getEmail());
                if (StringUtils.hasText(req.getPassword())) {
                    user.setPassword(pwdEncoder.encode(req.getPassword()));
                }
                userService.saveUser(user);
                return ResponseEntity.ok(response);
            }
        }
        // 如果使用者改了帳號，而且新的帳號已經存在，就回 409
        if (!newLoginId.equals(req.getOriginalLoginId()) && userService.existsById(newLoginId)) {
            response.put("message", "帳號已存在，請換一個");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        if (!newLoginId.equals(req.getOriginalLoginId())) {
            user.setLoginId(newLoginId);
        }
        user.setUsername(req.getUsername());
        user.setEmployee_no(req.getEmployeeNo());
        user.setEmail(req.getEmail());
        if (StringUtils.hasText(req.getPassword())) {
            user.setPassword(pwdEncoder.encode(req.getPassword()));
        }
        // 3. Department （必填）
        try {
            Department dept = departmentService.findByIdByDepartment(req.getDepartmentId());
            user.setDepartment(dept);
        } catch (NoSuchElementException ex) {
            throw new IllegalArgumentException("找不到部門 ID=" + req.getDepartmentId());
        }

        // 4. SubUnit （選填）
        if (req.getSubUnitId() != null) {
            try {
                SubUnit su = departmentService.findByIdBySubUnit(req.getSubUnitId());
                user.setSubUnit(su);
            } catch (NoSuchElementException ex) {
                throw new IllegalArgumentException("找不到子單位 ID=" + req.getSubUnitId());
            }
        } else {
            user.setSubUnit(null);
        }

        // 5. SubSubUnit （選填）
        if (req.getSubSubUnitId() != null) {
            try {
                SubSubUnit ssu = departmentService.findByIdBySubSubUnit(req.getSubSubUnitId());
                user.setSubSubUnit(ssu);
            } catch (NoSuchElementException ex) {
                throw new IllegalArgumentException("找不到子子單位 ID=" + req.getSubSubUnitId());
            }
        } else {
            user.setSubSubUnit(null);
        }

        // 6. Position （必填）
        try {
            Position pos = departmentService.findByIdByPosition(req.getPositionId());
            user.setPosition(pos);
        } catch (NoSuchElementException ex) {
            throw new IllegalArgumentException("找不到職位 ID=" + req.getPositionId());
        }
        //7.角色
        List<String> perms = req.getPermissions();
        if (perms != null) {
            // 清空舊的（orphanRemoval = true 會自動刪除對應的 row）
            user.getRoles().clear();

            // 以新的清單重建
            for (String roleStr : perms) {
                UserRole ur = new UserRole();
                ur.setUser(user);
                ur.setId(new UserRoleId(user.getId(), roleStr));
                user.getRoles().add(ur);
            }
        }
        userService.saveUser(user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/permission/toggle_user")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleUser(@RequestBody Map<String, String> body) {
        // 回傳新的狀態
        Map<String, Object> response = new HashMap<>();
        String loginId = body.get("loginId");
        User user = userService.getUser(loginId);
        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "找不到使用者 " + loginId));
        }
        boolean isAdmin = false;
        for (UserRole ur : user.getRoles()) {
            if ("ROLE_ADMIN".equals(ur.getId().getRole())) {
                isAdmin = true;
                break;
            }
        }
        if (isAdmin) {
            response.put("message", "最高管理者帳號無法變更權限");
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(response);
        }
        // 切換狀態
        user.setEnabled(!user.isEnabled());
        userService.saveUser(user);


        response.put("enabled", user.isEnabled());
        response.put("message", user.isEnabled() ? "帳號已啟用" : "帳號已停用");
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
            String loginId = jwtTokenProvider.getLoginIdFromToken(token);
            model.addAttribute("uri", request.getRequestURI());
            model.addAttribute("loginId", loginId);
            model.addAttribute("userName", userName);
            model.addAttribute("userRoles", roles);
            model.addAttribute("creator", userName);
        }
    }
}
