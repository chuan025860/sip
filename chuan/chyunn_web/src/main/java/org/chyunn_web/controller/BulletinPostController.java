package org.chyunn_web.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.chyunn_web.bean.Resource.BulletinPost;
import org.chyunn_web.security.JwtTokenProvider;
import org.chyunn_web.service.BulletinPostService;
import org.chyunn_web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class BulletinPostController {
    @Autowired
    UserService userService;
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    BulletinPostService bulletinPostService;

    @PostMapping("/resource_manage/saveBulletin")
    public ResponseEntity<?> saveBulletin(@RequestParam("board") String board,
                                          @RequestParam("title") String title,
                                          @RequestParam("publish_datetime") String publishDatetimeStr,
                                          @RequestParam("content") String content,
                                          @RequestParam("loginId") String loginId,
                                          @RequestParam(value = "attachments", required = false) MultipartFile[] attachments
    ) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime publishTime = LocalDateTime.parse(publishDatetimeStr, formatter);

            BulletinPost bulletinPost = new BulletinPost();
            bulletinPost.setTitle(title);
            bulletinPost.setContent(content);
            bulletinPost.setPublish_time(publishTime);
            bulletinPost.setCreated_at(LocalDateTime.now());
            bulletinPost.setCreator(userService.getUser(loginId));

            bulletinPostService.save(bulletinPost);

            Map<String, Object> res = new HashMap<>();
            res.put("id", bulletinPost.getId());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            e.printStackTrace(); // ✅ 印出錯誤日誌
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "儲存失敗：" + e.getMessage()));
        }
    }

    @PostMapping("/resource_manage/image")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("upload") MultipartFile file) {
        System.out.println("------------------------------");
        try {
            String uploadDir = "\\\\192.168.2.3\\群運共用夾\\※資訊部專用\\沈世泉\\bulletin\\ckeditor_images\\"; // 你可以改成 Linux 路徑
            String originalFilename = file.getOriginalFilename();
            String filename = UUID.randomUUID() + "_" + originalFilename;
            File saveFile = new File(uploadDir, filename);

            // 建立資料夾
            saveFile.getParentFile().mkdirs();
            file.transferTo(saveFile);

            // CKEditor 需要回傳 image URL（一定要有 "url" 欄位）
            String imageUrl = "/chyunn/uploads/ckeditor_images/" + filename;

            Map<String, Object> response = new HashMap<>();
            System.out.println(imageUrl);
            response.put("url", imageUrl);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Upload failed"));
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
            String loginId = jwtTokenProvider.getLoginIdFromToken(token);
            String userName = jwtTokenProvider.getUserNameFromToken(token);
            List<String> roles = jwtTokenProvider.getUserRolesFromToken(token);
            model.addAttribute("loginId", loginId);
            model.addAttribute("userName", userName);
            model.addAttribute("userRoles", roles);
            model.addAttribute("creator", userName);
        }
    }
}
