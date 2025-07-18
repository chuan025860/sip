package org.chyunn_web.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.chyunn_web.bean.Resource.BulletinFile;
import org.chyunn_web.bean.Resource.BulletinPost;
import org.chyunn_web.bean.Resource.EditorImageUsage;
import org.chyunn_web.bean.incident.IncidentFile;
import org.chyunn_web.dto.IncidentDto;
import org.chyunn_web.security.JwtTokenProvider;
import org.chyunn_web.service.BulletinFileService;
import org.chyunn_web.service.BulletinPostService;
import org.chyunn_web.service.IncidentService;
import org.chyunn_web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class BulletinPostController {
    @Autowired
    UserService userService;
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    BulletinPostService bulletinPostService;
    @Autowired
    BulletinFileService bulletinFileService;
    @Autowired
    IncidentService incidentService;

    @PostMapping("/resource_manage/saveBulletin")
    public ResponseEntity<?> saveBulletin(@RequestParam("board") String board,
                                          @RequestParam("title") String title,
                                          @RequestParam("publish_datetime") String publishDatetimeStr,
                                          @RequestParam("endDate") String endDatetimeStr,
                                          @RequestParam("content") String content,
                                          @RequestParam("loginId") String loginId,
                                          @RequestParam(value = "attachments", required = false) List<MultipartFile> attachments
    ) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime publishTime = LocalDateTime.parse(publishDatetimeStr, formatter);
            LocalDateTime endTime = LocalDateTime.parse(endDatetimeStr, formatter);
            Map<String, Object> response = new HashMap<>();
            BulletinPost bulletinPost = new BulletinPost();
            bulletinPost.setTitle(title);
            bulletinPost.setContent(content);
            bulletinPost.setPublishTime(publishTime);
            bulletinPost.setEndTime(endTime);
            bulletinPost.setCreated_at(LocalDateTime.now());
            bulletinPost.setCreator(userService.getUser(loginId));
            String contentHtml = bulletinPost.getContent();
            Pattern pattern = Pattern.compile("<img[^>]+src=[\"'](/chyunn/uploads/ckeditor_images/[^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(contentHtml);
            List<EditorImageUsage> imageUsages = new ArrayList<>();
            while (matcher.find()) {
                String fullPath = matcher.group(1);  // e.g. /chyunn/uploads/ckeditor_images/abc123.png
                String fileName = fullPath.substring(fullPath.lastIndexOf('/') + 1);  // e.g. abc123.png

                EditorImageUsage usage = new EditorImageUsage();
                usage.setImagePath(fileName);  // ✅ 只儲存檔名
                usage.setBulletinPost(bulletinPost);
                imageUsages.add(usage);
            }
            bulletinPost.setImageUsages(imageUsages);

            bulletinPostService.insert_bulletinPost_and_files(bulletinPost,attachments);

            Map<String, Object> res = new HashMap<>();
            res.put("id", bulletinPost.getId());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            e.printStackTrace(); // ✅ 印出錯誤日誌
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "儲存失敗：" + e.getMessage()));
        }
    }

    @PostMapping("/resource_manage/updateBulletin")
    public ResponseEntity<?> updateBulletin(@RequestParam("id") String id,
                                            @RequestParam("board") String board,
                                            @RequestParam("title") String title,
                                            @RequestParam("publish_datetime") String publishDatetimeStr,
                                            @RequestParam("content") String content,
                                            @RequestParam("loginId") String loginId,
                                            @RequestParam(value = "attachments", required = false)  List<MultipartFile> attachments
    ) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime publishTime = LocalDateTime.parse(publishDatetimeStr, formatter);
            Map<String, Object> response = new HashMap<>();
            BulletinPost bulletinPost = bulletinPostService.findById(Integer.valueOf(id));
            bulletinPost.setTitle(title);
            bulletinPost.setContent(content);
            bulletinPost.setPublishTime(publishTime);
            bulletinPost.setCreated_at(LocalDateTime.now());
            bulletinPost.setCreator(userService.getUser(loginId));

            String uploadDirBase = "\\\\192.168.2.3\\群運共用夾\\※資訊部專用\\沈世泉\\Bulletin_file\\";
            // 上傳檔案（若有）
            List<BulletinFile> uploadedFiles = new ArrayList<>();
            String eventFolderPath = uploadDirBase + "BulletinPost_" + bulletinPost.getId();
            // 用來追蹤已儲存的臨時檔案
            List<File> tempFiles = new ArrayList<>();
            File dir = new File(eventFolderPath);
            if (attachments != null && !attachments.isEmpty()) {
                if (!dir.exists() && !dir.mkdirs()) {
                    response.put("code", 500);
                    response.put("message", "無法建立附件存放目錄");
                    return ResponseEntity.badRequest().body(response);
                }
                // 定義最大檔案大小 10MB (10485760 字節)
                try {
                    // 儲存檔案並創建檔案記錄
                    for (MultipartFile file : attachments) {
                        if (!file.isEmpty()) {
                            String originalFilename = file.getOriginalFilename();
                            if (originalFilename != null) {
                                // 儲存檔案到事件資料夾
                                String filePath = eventFolderPath + File.separator + originalFilename;
                                File storedFile = new File(filePath);
                                file.transferTo(storedFile);
                                tempFiles.add(storedFile);  // 將已儲存的檔案加入列表

                                // 創建 IncidentFiles 物件並設定檔案路徑
                                BulletinFile bulletinFile = new BulletinFile();
                                bulletinFile.setFileName(originalFilename);
                                bulletinFile.setFilePath(filePath);
                                uploadedFiles.add(bulletinFile);
                            }
                        }
                    }
                } catch (IOException e) {
                    response.put("code", 500);
                    response.put("message", "檔案上傳失敗");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
            }
            String contentHtml = bulletinPost.getContent();

            Pattern pattern = Pattern.compile("<img[^>]+src=[\"'](/chyunn/uploads/ckeditor_images/[^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(contentHtml);
            List<EditorImageUsage> imageUsages = new ArrayList<>();
            while (matcher.find()) {
                String fullPath = matcher.group(1);  // e.g. /chyunn/uploads/ckeditor_images/abc123.png
                String fileName = fullPath.substring(fullPath.lastIndexOf('/') + 1);  // e.g. abc123.png
                EditorImageUsage usage = new EditorImageUsage();
                usage.setImagePath(fileName);  // ✅ 只儲存檔名
                usage.setBulletinPost(bulletinPost);
                imageUsages.add(usage);
            }
            bulletinPost.getImageUsages().clear();           // 清空原資料
            bulletinPost.getImageUsages().addAll(imageUsages);    // 加入新資料
            // 若有新檔案則存入
            if (!uploadedFiles.isEmpty()) {
                bulletinPost.getBulletinFiles().addAll(uploadedFiles);
            }

            bulletinPostService.update_bulletinPost_and_files(bulletinPost, bulletinPost.getBulletinFiles());

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

    @DeleteMapping("/resource_manage/bulletin_delete")
    public ResponseEntity<String> deleteBulletinPosts(@RequestBody List<Integer> ids) {
        try {
            bulletinPostService.deleteByIds(ids);
            return ResponseEntity.ok("成功刪除 " + ids.size() + " 筆資料");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("刪除失敗：" + e.getMessage());
        }
    }

    //刪除公佈欄_附件
    @DeleteMapping("/resource_manage/delete_file")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> incident_delete_file(@RequestParam String fileId, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 取得 token
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

            if (token == null || !jwtTokenProvider.validateToken(token)) {
                response.put("code", 401);
                response.put("message", "未授權的操作");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            String userName = jwtTokenProvider.getUserNameFromToken(token);
            List<String> roles = jwtTokenProvider.getUserRolesFromToken(token);
            boolean isAdmin = roles.contains("ROLE_ADMIN") || roles.contains("ROLE_BULLETIN_ADMIN");
            BulletinFile bulletinFile = bulletinFileService.getBulletinFile(fileId);
            // 只有上傳者本人或管理員可刪
            if (!isAdmin && !bulletinFile.getBulletinPost().getCreator().equals(userName)) {
                response.put("code", 403);
                response.put("message", "只有檔案上傳者或管理員可以刪除檔案");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            // 先刪除資料庫紀錄
            bulletinFileService.deleteFile(bulletinFile);
            Path filePath = Paths.get(bulletinFile.getFilePath());
            Files.deleteIfExists(filePath); // 確保檔案存在時才刪除
            response.put("code", 200);
            response.put("message", "File deleted successfully.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("code", 500);
            response.put("message", "伺服器錯誤，請稍後再試");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
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
            model.addAttribute("uri", request.getRequestURI());
            model.addAttribute("loginId", loginId);
            model.addAttribute("userName", userName);
            model.addAttribute("userRoles", roles);
            model.addAttribute("creator", userName);
        }
    }
}
