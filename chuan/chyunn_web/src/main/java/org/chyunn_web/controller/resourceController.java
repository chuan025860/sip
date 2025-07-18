package org.chyunn_web.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.chyunn_web.bean.Resource.BulletinFile;
import org.chyunn_web.bean.Resource.BulletinPost;
import org.chyunn_web.bean.Resource.MeetingRoom;
import org.chyunn_web.bean.Resource.Vehicle;
import org.chyunn_web.dto.BulletinFileDto;
import org.chyunn_web.dto.BulletinPostDto;
import org.chyunn_web.security.JwtTokenProvider;
import org.chyunn_web.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
public class resourceController {
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    ResourceBorrowRequestServie resourceBorrowRequestServie;
    @Autowired
    BulletinPostService bulletinPostService;
    @Autowired
    UserService userService;
    @Autowired
    MeetingRoomService meetingRoomService;
    @Autowired
    VehicleService vehicleService;

    @GetMapping("/resource/calendar")
    public String calendar(
    ) {
        return "resourceManagement/calendar";
    }

    @GetMapping("/resource/bulletin")
    public String bulletin(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
                           @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishTime").descending());
        Page<BulletinPost> bulletinPage = bulletinPostService.findAllOrderByCreatedAtDesc(pageable);

        List<BulletinPostDto> bulletinPostDtos = bulletinPage.getContent().stream()
                .map(post -> {
                    BulletinPostDto dto = bulletinPostService.convertBulletinPostDto(post);
                    dto.setPublishTimeStr(post.getPublishTime().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")));
                    dto.setCreatorName(post.getCreator().getUsername());
                    return dto;
                })
                .collect(Collectors.toList());

        model.addAttribute("bulletinPostDtos", bulletinPostDtos);
        model.addAttribute("totalPages", bulletinPage.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalElements", bulletinPage.getTotalElements());
        model.addAttribute("pageSize", size);

        return "resourceManagement/bulletin";
    }

    @GetMapping("/resource/bulletinDetail")
    public String BulletinDetail(@RequestParam Integer id, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request
    ) {
        try {
//            bulletinPostService.cleanUnusedImages();
            BulletinPost bulletinPost = bulletinPostService.findById(id);
            if (bulletinPost == null) {
                throw new RuntimeException("找不到指定的公告資料");
            }
            // 檢查是否已過期
            LocalDateTime now = LocalDateTime.now();
            boolean isExpired = bulletinPost.getEndTime().isBefore(now);
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
            String currentLoginId = jwtTokenProvider.getLoginIdFromToken(token);
            // 檢查是否為管理員
            boolean isAdmin = userService.getUser(currentLoginId).getRoles().stream()
                    .map(userRole -> userRole.getId().getRole())
                    .anyMatch(roleName ->
                            "ROLE_ADMIN".equals(roleName) || "ROLE_BULLETIN_ADMIN".equals(roleName)
                    );

            if (isExpired && !isAdmin) {
                throw new RuntimeException("此公告已過期，無法瀏覽。");
            }
            String contentHtml = bulletinPost.getContent();
            Pattern pattern = Pattern.compile("<img[^>]+src=[\"'](/chyunn/uploads/ckeditor_images/[^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(contentHtml);

            while (matcher.find()) {
                String fullPath = matcher.group(1);  // e.g. /chyunn/uploads/ckeditor_images/abc123.png
                String fileName = fullPath.substring(fullPath.lastIndexOf('/') + 1);  // e.g. abc123.png

                System.out.println(fileName);
            }
            BulletinPostDto bulletinPostDto = bulletinPostService.convertBulletinPostDto(bulletinPost);
            List<BulletinFileDto> bulletinFileDtos = new ArrayList<>();
            for (BulletinFile bulletinFile : bulletinPost.getBulletinFiles()) {
                BulletinFileDto bulletinFileDto = bulletinPostService.convertBulletinFileDto(bulletinFile);
                bulletinFileDto.setBulletinId(bulletinPostDto.getId());
                bulletinFileDtos.add(bulletinFileDto);
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
            bulletinPostDto.setPublishTimeStr(bulletinPost.getPublishTime().format(formatter));
            bulletinPostDto.setEndTimeStr(bulletinPost.getEndTime().format(formatter));
            bulletinPostDto.setCreatorName(bulletinPost.getCreator().getUsername());
            bulletinPostDto.setBulletinFileDtos(bulletinFileDtos);

            model.addAttribute("bulletinPost", bulletinPostDto);
            return "resourceManagement/bulletinDetail";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/resource/bulletin";
        }
    }

    @GetMapping("/resource_manage/insertBulletin")
    public String insertBulletin(
    ) {
        return "resourceManagement/insertBulletin";
    }

    @GetMapping("/resource_manage/editBulletin")
    public String updateBulletin(@RequestParam Integer id, Model model
    ) {
        BulletinPost bulletinPost = bulletinPostService.findById(id);
        BulletinPostDto bulletinPostDto = bulletinPostService.convertBulletinPostDto(bulletinPost);
        List<BulletinFileDto> bulletinFileDtos = new ArrayList<>();
        for (BulletinFile bulletinFile : bulletinPost.getBulletinFiles()) {
            BulletinFileDto bulletinFileDto = bulletinPostService.convertBulletinFileDto(bulletinFile);
            bulletinFileDto.setBulletinId(bulletinPostDto.getId());
            bulletinFileDtos.add(bulletinFileDto);
        }
        bulletinPostDto.setBulletinFileDtos(bulletinFileDtos);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
        bulletinPostDto.setPublishTimeStr(bulletinPost.getPublishTime().format(formatter));
        bulletinPostDto.setCreatorName(bulletinPost.getCreator().getUsername());
        model.addAttribute("bulletinPostDto", bulletinPostDto);
        return "resourceManagement/editBulletin";
    }

    @GetMapping("/resource/resources")
    public String resources(
    ) {
        return "resourceManagement/resources";
    }


    @GetMapping("/resource/latest-bulletins")
    @ResponseBody
    public List<Map<String, Object>> getLatestBulletins() {
        List<BulletinPost> posts = bulletinPostService.findTop5ByOrderByPublish_timeDesc(); // 取最新 5 筆
        List<Map<String, Object>> result = new ArrayList<>();

        for (BulletinPost post : posts) {
            Map<String, Object> item = new HashMap<>();
            item.put("title", post.getTitle());
            item.put("background", post.getImageUsages().isEmpty()
                    ? "/images/default.jpg" // 預設背景
                    : "/chyunn/uploads/ckeditor_images/" + post.getImageUsages().get(0).getImagePath());
            item.put("content", post.getContent());
            // 處理附件檔案清單
            List<Map<String, String>> fileList = new ArrayList<>();
            if (post.getBulletinFiles() != null) {
                for (BulletinFile file : post.getBulletinFiles()) {
                    Map<String, String> fileMap = new HashMap<>();
                    fileMap.put("fileName", file.getFileName());  // 檔名
                    fileMap.put("fileUrl", "/chyunn/uploads/BulletinPost_" + file.getBulletinPost().getId() + "/" + file.getFileName());  // 前端 URL
                    fileList.add(fileMap);
                }
            }
            item.put("files", fileList);

            result.add(item);
        }

        return result;
    }

    @GetMapping("/resource/resourceMaintenance")
    public String resourceMaintenance() {
        return "resourceManagement/resourceMaintenance";
    }

    @GetMapping("/permission/resource-category/{code}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resource_category(@PathVariable String code) {
        Map<String, Object> result = new HashMap<>();
        if ("ROOM".equals(code)) {
            List<MeetingRoom> rooms = meetingRoomService.findAllMeetingRoom();
            result.put("data", rooms);
        } else if ("VEHICLE".equals(code)) {
            List<Vehicle> vehicles = vehicleService.findAll();
            result.put("data", vehicles);
        } else {
            result.put("data", Collections.emptyList());
        }

        return ResponseEntity.ok(result);
    }

    @PutMapping("/permission/resource-category-update/{code}/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resource_category_update(@PathVariable String code, @PathVariable String id, @RequestBody Map<String, String> requestBody) {
        Map<String, Object> result = new HashMap<>();
        String name = requestBody.get("name");

        try {
            if ("ROOM".equals(code)) {
                MeetingRoom meetingRoom = meetingRoomService.findMeetingRoomById(Integer.parseInt(id));
                meetingRoom.setName(name);
                meetingRoomService.insertMeetingRoom(meetingRoom);
            } else if ("VEHICLE".equals(code)) {
                Vehicle vehicle = vehicleService.findVehicleById(id);
                vehicle.setName(name);
                vehicleService.insertVehicle(vehicle);
            } else {
                result.put("success", false);
                result.put("message", "不支援的類別");
                return ResponseEntity.badRequest().body(result);
            }

            result.put("success", true);
            result.put("message", "更新成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失敗：" + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/permission/resource-category-delete/{code}/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resource_category_delete(@PathVariable String code, @PathVariable String id) {
        Map<String, Object> result = new HashMap<>();

        try {
            if ("ROOM".equals(code)) {
                MeetingRoom meetingRoom = meetingRoomService.findMeetingRoomById(Integer.parseInt(id));
                meetingRoomService.deleteMeetingRoom(meetingRoom);
            } else if ("VEHICLE".equals(code)) {
                Vehicle vehicle = vehicleService.findVehicleById(id);
                vehicleService.deleteVehicle(vehicle);
            } else {
                result.put("success", false);
                result.put("message", "不支援的類別");
                return ResponseEntity.badRequest().body(result);
            }
            result.put("success", true);
            result.put("message", "刪除成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "刪除失敗：" + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/permission/resource-category-add/{code}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resource_category_add(@PathVariable String code, @RequestBody Map<String, String> requestBody) {
        Map<String, Object> result = new HashMap<>();
        String name = requestBody.get("name");
        try {
            if ("ROOM".equals(code)) {
                MeetingRoom meetingRoom = new MeetingRoom();
                meetingRoom.setName(name);
                meetingRoomService.insertMeetingRoom(meetingRoom);
            } else if ("VEHICLE".equals(code)) {
                Vehicle vehicle = new Vehicle();
                vehicle.setName(name);
                vehicleService.insertVehicle(vehicle);
            } else {
                result.put("success", false);
                result.put("message", "不支援的類別");
                return ResponseEntity.badRequest().body(result);
            }
            result.put("success", true);
            result.put("message", "新增成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "新增失敗：" + e.getMessage());
        }

        return ResponseEntity.ok(result);
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
