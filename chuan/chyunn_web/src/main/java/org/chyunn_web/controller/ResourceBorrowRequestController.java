package org.chyunn_web.controller;

import org.chyunn_web.bean.Resource.ResourceBorrowRequest;
import org.chyunn_web.bean.User.User;
import org.chyunn_web.dto.EventDTO;
import org.chyunn_web.dto.ResourceBorrowDTO;
import org.chyunn_web.service.ResourceBorrowRequestServie;
import org.chyunn_web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ResourceBorrowRequestController {
    @Autowired
    ResourceBorrowRequestServie resourceBorrowRequestServie;
    @Autowired
    UserService userService;

    @PostMapping("/resource/resource-borrow")
    public ResponseEntity<Map<String, Object>> createBorrowRequest(@RequestBody ResourceBorrowDTO dto) {
        User creator = userService.getUser(dto.getCreator());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        Map<String, Object> response = new HashMap<>();
        ResourceBorrowRequest request = new ResourceBorrowRequest();
        request.setContent(dto.getContent());
        request.setStartTime(LocalDateTime.parse(dto.getStartTime(), formatter));
        request.setEndTime(LocalDateTime.parse(dto.getEndTime(), formatter));
        request.setCategory(dto.getCategory());
        request.setItem(dto.getItem());
        request.setLocation(dto.getLocation());
        request.setNote(dto.getNote());
        request.setParticipants(dto.getParticipants());
        request.setCreator(creator);
        request.setCreatedAt(LocalDateTime.now());
        resourceBorrowRequestServie.saveResourceBorrowr(request);
        response.put("code", 200);
        return ResponseEntity.ok(response); // 返回成功的 response
    }

    @PostMapping("/resource/events")
    public ResponseEntity<Map<String, Object>> events() {
        Map<String, Object> response = new HashMap<>();
        List<ResourceBorrowRequest> resourceBorrowRequestList = resourceBorrowRequestServie.findAll();
        List<EventDTO> eventDTOS = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        for (ResourceBorrowRequest resourceBorrowRequest : resourceBorrowRequestList) {
            EventDTO eventDTO = new EventDTO();
            eventDTO.setTitle(resourceBorrowRequest.getContent());
            eventDTO.setStart(resourceBorrowRequest.getStartTime().format(formatter));
            eventDTO.setEnd(resourceBorrowRequest.getEndTime().format(formatter));
            eventDTO.setAllDay(false);
            eventDTO.setLocation(resourceBorrowRequest.getLocation());
            eventDTO.setNote(resourceBorrowRequest.getNote());
            eventDTO.setParticipants(resourceBorrowRequest.getParticipants());
            eventDTO.setCreator(resourceBorrowRequest.getCreator().getLoginId());
            eventDTO.setCategory(resourceBorrowRequest.getCategory());
            eventDTO.setItem(resourceBorrowRequest.getItem());
            // 根據 item 設定顏色
            String item = resourceBorrowRequest.getItem();
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
            @RequestParam @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="10") int size
    ) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end   = endDate.atTime(23,59,59);
        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").descending());

        Page<ResourceBorrowRequest> prs = resourceBorrowRequestServie.findByDateRange(start, end, pageable);
        // 將 Page<Request> 轉成 Page<DTO>
        return prs.map(req -> {
            ResourceBorrowDTO dto = new ResourceBorrowDTO();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            dto.setId(req.getId());
            dto.setContent(req.getContent());
            dto.setCategory(req.getCategory());
            dto.setItem(req.getItem());
            dto.setLocation(req.getLocation());
            dto.setNote(req.getNote());
            dto.setParticipants(req.getParticipants());
            dto.setCreator(req.getCreator().getUsername());
            dto.setStartTime(req.getStartTime().format(fmt));
            dto.setEndTime(req.getEndTime().format(fmt));
            dto.setCreatedAt(req.getCreatedAt().format(fmt));
            long daysOld = ChronoUnit.DAYS.between(LocalDateTime.now(), req.getStartTime());
            dto.setDaysOld(daysOld);
            return dto;
        });
    }
}
