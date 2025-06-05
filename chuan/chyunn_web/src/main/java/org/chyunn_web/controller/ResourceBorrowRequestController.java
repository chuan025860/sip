package org.chyunn_web.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.chyunn_web.bean.Resource.MeetingRoom;
import org.chyunn_web.bean.Resource.ResourceBorrowRequest;
import org.chyunn_web.bean.Resource.Vehicle;
import org.chyunn_web.bean.User.User;
import org.chyunn_web.dto.EventDTO;
import org.chyunn_web.dto.MeetingRoomDto;
import org.chyunn_web.dto.ResourceBorrowDTO;
import org.chyunn_web.dto.VehicleDto;
import org.chyunn_web.security.JwtTokenProvider;
import org.chyunn_web.service.MeetingRoomService;
import org.chyunn_web.service.ResourceBorrowRequestServie;
import org.chyunn_web.service.UserService;
import org.chyunn_web.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ResourceBorrowRequestController {
    @Autowired
    ResourceBorrowRequestServie resourceBorrowRequestServie;
    @Autowired
    UserService userService;
    @Autowired
    MeetingRoomService meetingRoomService;
    @Autowired
    VehicleService vehicleService;
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @PostMapping("/resource/resource-borrow")
    public ResponseEntity<Map<String, Object>> createBorrowRequest(@RequestBody ResourceBorrowDTO dto) {
        User creator = userService.getUser(dto.getCreator());
        System.out.println(creator.getUsername());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        Map<String, Object> response = new HashMap<>();
        ResourceBorrowRequest request = new ResourceBorrowRequest();
        request.setContent(dto.getContent());
        if (dto.isAllDay()) {
            LocalDate startDate = LocalDate.parse(dto.getStartTime(), DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate endDate   = LocalDate.parse(dto.getEndTime(),   DateTimeFormatter.ISO_LOCAL_DATE);
            request.setStartTime(startDate.atStartOfDay());
            request.setEndTime(endDate.atTime(23, 59, 59));
            request.setAllDay(true);
        } else {
            // 一般非全天：解析「yyyy-MM-dd HH:mm」
            request.setStartTime(LocalDateTime.parse(dto.getStartTime(), formatter));
            request.setEndTime(  LocalDateTime.parse(dto.getEndTime(),   formatter));
            request.setAllDay(false);
        }
        request.setCategory(ResourceBorrowRequest.Category.valueOf(dto.getCategory()));
        request.setLocation(dto.getLocation());
        request.setNote(dto.getNote());
        request.setParticipants(dto.getParticipants());
        request.setCreator(creator);
        request.setCreatedAt(LocalDateTime.now());
        if ("ROOM".equalsIgnoreCase(dto.getCategory())) {
            request.setCategory(ResourceBorrowRequest.Category.ROOM);
            request.setMeetingRoom(meetingRoomService.findMeetingRoomById(Integer.parseInt(dto.getFinalItem())));
        } else if ("VEHICLE".equalsIgnoreCase(dto.getCategory())) {
            request.setCategory(ResourceBorrowRequest.Category.VEHICLE);
            request.setVehicle(vehicleService.findVehicleById(dto.getFinalItem()));
        }else if("OTHER".equals(dto.getCategory())){
            request.setItem_other(dto.getFinalItem());
        }
       resourceBorrowRequestServie.saveResourceBorrowr(request);
        response.put("code", 200);
        return ResponseEntity.ok(response); // 返回成功的 response
    }

    @PostMapping("/resource/events")
    public ResponseEntity<Map<String, Object>> events(HttpServletRequest request) {
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
        String loginId = jwtTokenProvider.getLoginIdFromToken(token);// 登入者id
        List<String> roles = jwtTokenProvider.getUserRolesFromToken(token); // 取得角色
        String currentRole = roles.contains("ROLE_ADMIN") ? "ROLE_ADMIN" : "USER"; // 你可以只塞 ADMIN
        Map<String, Object> response = new HashMap<>();
        List<ResourceBorrowRequest> resourceBorrowRequestList = resourceBorrowRequestServie.findAll();
        List<EventDTO> eventDTOS = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        for (ResourceBorrowRequest resourceBorrowRequest : resourceBorrowRequestList) {
            LocalDateTime start = resourceBorrowRequest.getStartTime();
            LocalDateTime end   = resourceBorrowRequest.getEndTime();
            boolean crossDay = !start.toLocalDate().equals(end.toLocalDate());
            boolean allDay = resourceBorrowRequest.getAllDay() || crossDay;
            EventDTO eventDTO = new EventDTO();
            // 取得 item 名稱
            String item = "";
            if (resourceBorrowRequest.getCategory() == ResourceBorrowRequest.Category.ROOM && resourceBorrowRequest.getMeetingRoom() != null) {
                item = resourceBorrowRequest.getMeetingRoom().getName();
            } else if (resourceBorrowRequest.getCategory() == ResourceBorrowRequest.Category.VEHICLE && resourceBorrowRequest.getVehicle() != null) {
                item = resourceBorrowRequest.getVehicle().getName();
            } else if (resourceBorrowRequest.getCategory() == ResourceBorrowRequest.Category.OTHER) {
                item = resourceBorrowRequest.getItem_other();
            }
            eventDTO.setTitle(resourceBorrowRequest.getContent() + (item != null ? " - " + item : ""));
            if (allDay) {
                // 全天事件：從當天 00:00 開始，到「最後一天 + 1」的 00:00
                LocalDate sd = start.toLocalDate();
                LocalDate ed = end.toLocalDate().plusDays(1);
                eventDTO.setStart(sd.atStartOfDay().format(formatter));
                eventDTO.setEnd(ed.atStartOfDay().format(formatter));
            } else {
                // 普通時段
                eventDTO.setStart(start.format(formatter));
                eventDTO.setEnd(  end.format(formatter));
            }
            eventDTO.setAllDay(resourceBorrowRequest.getAllDay() || crossDay);
            eventDTO.setLocation(resourceBorrowRequest.getLocation());
            eventDTO.setNote(resourceBorrowRequest.getNote());
            eventDTO.setParticipants(resourceBorrowRequest.getParticipants());
            eventDTO.setCreator(resourceBorrowRequest.getCreator().getUsername());
            eventDTO.setCreatorId(resourceBorrowRequest.getCreator().getLoginId());
            eventDTO.setCategory(String.valueOf(resourceBorrowRequest.getCategory()));
            eventDTO.setItem(item);
            eventDTO.setId(String.valueOf(resourceBorrowRequest.getId()));
            eventDTO.setCurrentUser(loginId);
            eventDTO.setCurrentRole(currentRole);
            // 根據 item 設定顏色
            switch (item) {
                case "群運營運中心一樓小會議室(116)":
                    eventDTO.setBackgroundColor("#1abc9c");
                    eventDTO.setBorderColor("#1abc9c");
                    break;
                case "群運營運中心二樓洽談室(202)":
                    eventDTO.setBackgroundColor("#3498db");
                    eventDTO.setBorderColor("#3498db");
                    break;
                case "群運安明一樓會議室":
                    eventDTO.setBackgroundColor("#e67e22");
                    eventDTO.setBorderColor("#e67e22");
                    break;
                case "群運營運中心二樓交誼廳":
                    eventDTO.setBackgroundColor("#9b59b6");
                    eventDTO.setBorderColor("#9b59b6");
                    break;
                case "群運營運中心一樓大會議室(103)":
                    eventDTO.setBackgroundColor("#2ecc71");
                    eventDTO.setBorderColor("#2ecc71");
                    break;
                case "營業部公務車":
                    eventDTO.setBackgroundColor("#f1c40f");
                    eventDTO.setBorderColor("#f1c40f");
                    break;
                case "行政部公務車":
                    eventDTO.setBackgroundColor("#e74c3c");
                    eventDTO.setBorderColor("#e74c3c");
                    break;
                default:
                    eventDTO.setBackgroundColor("#95a5a6"); // 預設灰色
                    eventDTO.setBorderColor("#95a5a6");
                    break;
            }
            eventDTOS.add(eventDTO);

        }
        response.put("code", 200);
        response.put("data", eventDTOS);
        return ResponseEntity.ok(response); // 返回成功的 response
    }

    // AJAX API：依日期與分頁回 JSON
    @GetMapping("/resource/eventsByDate")
    @ResponseBody
    public Page<ResourceBorrowDTO> eventsByDate(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,HttpServletRequest request
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
        String loginId = jwtTokenProvider.getLoginIdFromToken(token);// 登入者id
        List<String> roles = jwtTokenProvider.getUserRolesFromToken(token); // 取得角色
        String currentRole = roles.contains("ROLE_ADMIN") ? "ROLE_ADMIN" : "USER"; // 你可以只塞 ADMIN
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").descending());

        Page<ResourceBorrowRequest> prs = resourceBorrowRequestServie.findByDateRange(start, end, pageable);
        for (ResourceBorrowRequest resourceBorrowRequest : prs.getContent()) {
            System.out.println(resourceBorrowRequest.getCreator().getUsername());
        }

        // 將 Page<Request> 轉成 Page<DTO>
        return prs.map(req -> {
            ResourceBorrowDTO dto = new ResourceBorrowDTO();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            dto.setId(req.getId());
            dto.setContent(req.getContent());
            dto.setCategory(req.getCategory().toString());
            if (req.getCategory() == ResourceBorrowRequest.Category.ROOM) {
                if (req.getMeetingRoom() != null) {

                    dto.setFinalItem(req.getMeetingRoom().getName());
                }
            } else if (req.getCategory() == ResourceBorrowRequest.Category.VEHICLE) {
                if (req.getVehicle() != null) {
                    dto.setFinalItem(req.getVehicle().getName());
                }
            }else if (req.getCategory() == ResourceBorrowRequest.Category.OTHER) {
                dto.setFinalItem(req.getItem_other());
            }
            dto.setLocation(req.getLocation());
            dto.setNote(req.getNote());
            dto.setParticipants(req.getParticipants());
            dto.setCreatorId(req.getCreator().getLoginId());
            dto.setCreator(req.getCreator().getUsername());
            dto.setStartTime(req.getStartTime().format(fmt));
            dto.setEndTime(req.getEndTime().format(fmt));
            dto.setCreatedAt(req.getCreatedAt().format(fmt));
            dto.setCurrentUser(loginId);
            dto.setCurrentRole(currentRole);
            long daysOld = ChronoUnit.DAYS.between(LocalDateTime.now(), req.getStartTime());
            dto.setDaysOld(daysOld);
            return dto;
        });
    }

    @GetMapping("/resource/getResource")
    @ResponseBody
    public ResourceBorrowDTO getResource(
            @RequestParam int id
    ) {
        ResourceBorrowRequest request = resourceBorrowRequestServie.getResourceBorrowRequestById(id);
        ResourceBorrowDTO dto = new ResourceBorrowDTO();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        dto.setId(request.getId());
        dto.setContent(request.getContent());
        dto.setCategory(String.valueOf(request.getCategory()));
        if (request.getCategory() == ResourceBorrowRequest.Category.ROOM) {
            if (request.getMeetingRoom() != null) {
                dto.setFinalItem(request.getMeetingRoom().getName());
                dto.setFinalItemId(String.valueOf(request.getMeetingRoom().getId()));
            }
        } else if (request.getCategory() == ResourceBorrowRequest.Category.VEHICLE) {
            if (request.getVehicle() != null) {
                dto.setFinalItem(request.getVehicle().getName());
                dto.setFinalItemId(String.valueOf(request.getVehicle().getId()));
            }
        }else if (request.getCategory() == ResourceBorrowRequest.Category.OTHER) {
            dto.setFinalItem(request.getItem_other());
        }
        dto.setLocation(request.getLocation());
        dto.setNote(request.getNote());
        dto.setAllDay(request.getAllDay());
        dto.setParticipants(request.getParticipants());
        dto.setCreator(request.getCreator().getUsername());
        dto.setStartTime(request.getStartTime().format(fmt));
        dto.setEndTime(request.getEndTime().format(fmt));
        dto.setCreatedAt(request.getCreatedAt().format(fmt));
        return dto;
    }

    @PutMapping("/resource/{id}")
    public ResponseEntity<Map<String, Object>> updateBorrowRequest(
            @PathVariable Integer id,
            @RequestBody ResourceBorrowDTO dto) {

        Map<String, Object> resp = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        // 1) 取出既有紀錄
        ResourceBorrowRequest existing = resourceBorrowRequestServie.getResourceBorrowRequestById(id);
        if (existing == null) {
            resp.put("message", "找不到借用紀錄 id=" + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }

        try {
            // 2) 更新欄位
            existing.setContent(dto.getContent());
            System.out.println(dto.getFinalItem());
            if ("ROOM".equalsIgnoreCase(dto.getCategory())) {
                existing.setCategory(ResourceBorrowRequest.Category.ROOM);
                existing.setMeetingRoom(meetingRoomService.findMeetingRoomById(Integer.parseInt(dto.getFinalItem())));
                existing.setVehicle(null);
                existing.setItem_other(null);
            } else if ("VEHICLE".equalsIgnoreCase(dto.getCategory())) {
                existing.setCategory(ResourceBorrowRequest.Category.VEHICLE);
                existing.setVehicle(vehicleService.findVehicleById(dto.getFinalItem()));
                existing.setMeetingRoom(null);
                existing.setItem_other(null);
            }else if("OTHER".equals(dto.getCategory())){
                existing.setCategory(ResourceBorrowRequest.Category.OTHER);
                existing.setVehicle(null);
                existing.setMeetingRoom(null);
                existing.setItem_other(dto.getFinalItem());
            }
            existing.setLocation(dto.getLocation());
            existing.setNote(dto.getNote());
            existing.setParticipants(dto.getParticipants());
            // 3) 處理時間
            if (dto.isAllDay()) {
                // 全天事件：parse yyyy-MM-dd
                LocalDate startDate = LocalDate.parse(dto.getStartTime(), DateTimeFormatter.ISO_LOCAL_DATE);
                LocalDate endDate   = LocalDate.parse(dto.getEndTime(),   DateTimeFormatter.ISO_LOCAL_DATE);
                existing.setStartTime(startDate.atStartOfDay());
                existing.setEndTime(endDate.atTime(23, 59, 59));
                existing.setAllDay(true);

            } else {
                existing.setStartTime(LocalDateTime.parse(dto.getStartTime(), formatter));
                existing.setEndTime(  LocalDateTime.parse(dto.getEndTime(),   formatter));
                existing.setAllDay(false);
            }
            // 4) 存檔
            resourceBorrowRequestServie.saveResourceBorrowr(existing);
            resp.put("message", "更新成功");
            return ResponseEntity.ok(resp);

        } catch (DateTimeParseException ex) {
            resp.put("message", "時間格式錯誤");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
        } catch (Exception ex) {
            resp.put("message", "伺服器錯誤");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    @GetMapping("/resource/available")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> listRooms(     @RequestParam String category,
                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        System.out.println(category);

        System.out.println(start);
        System.out.println(end);
        if (category.equals("ROOM")) {
            List<MeetingRoom> rooms = meetingRoomService.getAvailableRooms(start, end);
            List<Map<String, Object>> result = rooms.stream().map(room -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", room.getId());
                map.put("name", room.getName());
                return map;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(result);
        }else if(category.equals("VEHICLE")){
            List<Vehicle>vehicles=vehicleService.getAvailableVehicles(start, end);
            List<Map<String, Object>> result = vehicles.stream().map(room -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", room.getId());
                map.put("name", room.getName());
                return map;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().build();
    }


    @GetMapping("/resource/check-available")
    @ResponseBody
    public ResponseEntity<String> checkItemAvailability(
            @RequestParam String category,
            @RequestParam Integer id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(required = false) Integer excludeRequestId // 編輯時排除自己這筆
    ) {

        ResourceBorrowRequest req = resourceBorrowRequestServie.getResourceBorrowRequestById(id);
        System.out.println(category);
        System.out.println(req.getId());
//        System.out.println(id);
        boolean available;
        switch (category) {
            case "ROOM":
                if (req.getMeetingRoom() == null) {
                    return ResponseEntity.badRequest().body("該筆資料沒有綁定會議室");
                }
                Integer roomId = req.getMeetingRoom().getId();
                available = meetingRoomService.isAvailable(roomId, start, end, excludeRequestId);
                break;
            case "VEHICLE":
                if (req.getVehicle() == null) {
                    System.out.println("tetest");
                    return ResponseEntity.badRequest().body("該筆資料沒有綁定車輛");
                }
                Integer vehicleId = req.getVehicle().getId();
                available = vehicleService.isAvailable(vehicleId, start, end, excludeRequestId);
                break;
            default:
                return ResponseEntity.badRequest().body("無效類別");
        }
        return available
                ? ResponseEntity.ok("OK")
                : ResponseEntity.status(HttpStatus.CONFLICT).body("該時間段已被占用");
    }


}
