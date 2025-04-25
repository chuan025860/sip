package org.chyunn_web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.chyunn_web.bean.Incident;
import org.chyunn_web.bean.IncidentFile;
import org.chyunn_web.bean.Report;
import org.chyunn_web.bean.ReportFile;
import org.chyunn_web.dto.IncidentDto;
import org.chyunn_web.dto.IncidentFileDto;
import org.chyunn_web.dto.ReportDto;
import org.chyunn_web.dto.ReportFileDto;
import org.chyunn_web.security.JwtTokenProvider;
import org.chyunn_web.service.IncidentFilesService;
import org.chyunn_web.service.IncidentService;
import org.chyunn_web.service.ReportFilesService;
import org.chyunn_web.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Controller
public class IncidentController {
    @Autowired
    IncidentService incidentService;
    @Autowired
    ReportService reportService;
    @Autowired
    ReportFilesService reportFilesService;
    @Autowired
    IncidentFilesService incidentFilesService;
    @Autowired
    JwtTokenProvider jwtTokenProvider;


    //轉去事件追蹤頁
    @GetMapping("/incident/select_incident")
    public String into_select_incident(
    ) {
        return "incident/select_incident";
    }

    @PostMapping("/incident/select_incident")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> select_incident(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Incident> incidentPage = incidentService.findAll(pageable);
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            List<IncidentDto> incidentDtos = new ArrayList<>();

            for (Incident incident : incidentPage.getContent()) {
                IncidentDto incidentDto = incidentService.convertIncidentToIncidentDto(incident);
                int totalFiles = incident.getIncidentFiles() != null ? incident.getIncidentFiles().size() : 0;

                totalFiles += incident.getReports().stream()
                        .mapToInt(report -> report.getReportFiles() != null ? report.getReportFiles().size() : 0)
                        .sum();

                incidentDto.setTotalFiles(totalFiles);
                incidentDto.setLastReportTime(incident.getLastReportTime().format(dateTimeFormatter));
                incidentDto.setReportDate(simpleDateFormat.format(incident.getReportDate()));
                incidentDtos.add(incidentDto);
            }

            response.put("content", incidentDtos);
            response.put("totalPages", incidentPage.getTotalPages());
            response.put("totalElements", incidentPage.getTotalElements());
            response.put("currentPage", page);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace(); // 可選：印出錯誤堆疊以利除錯
            response.put("code", 500);
            response.put("message", "查詢事件時發生錯誤：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    //轉去事件新增頁
    @GetMapping("/incident/inster_incident")
    public String into_inster_incident() {
        return "incident/inster_incident";
    }

    //事件新增
    @PostMapping("/incident/inster_incident")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> insterIncident(
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestPart("incidentJson") String incidentJson) {

        Map<String, Object> response = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        Incident incident;
        Incident newIncident;
        try {
            // 解析 JSON 字符串為 Incident 物件
            incident = objectMapper.readValue(incidentJson, Incident.class);
            incident.setReportCount(0);
            incident.setLastReportTime(LocalDateTime.now());
        } catch (JsonProcessingException e) {
            response.put("code", 400);
            response.put("message", "JSON 解析錯誤");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        try {
            newIncident = incidentService.insert_incident_and_files(incident, files);
            Map<String, Object> data = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            data.put("incident", newIncident);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("code", 500);
            response.put("message", "伺服器錯誤，請稍後再試");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    //轉去事件明細
    @GetMapping("/incident/incident_Detail")
    public String incident_Detail(@RequestParam Integer incidentId, Model model) throws JsonProcessingException {
        Incident incident = incidentService.getIncident(incidentId);
        IncidentDto incidentDto = incidentService.convertIncidentToIncidentDto(incident);
        incidentDto.setMaxIncidentId(incidentService.findMaxIncidentId());

        // 1. 轉換事件附件
        List<IncidentFileDto> incidentFileDtos = new ArrayList<>();
        for (IncidentFile incidentFile : incident.getIncidentFiles()) {
            IncidentFileDto incidentFileDto = incidentFilesService.convertIncidentFileToIncidentFileDto(incidentFile);
            incidentFileDto.setIncidentId(incident.getIncidentId());
            incidentFileDtos.add(incidentFileDto);
        }
        incidentDto.setIncidentFileDtos(incidentFileDtos);

        // 2. 轉換回報 & 附件
        List<ReportDto> reportDtos = new ArrayList<>();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        for (Report report : incident.getReports()) {
            // 轉換報告附件
            List<ReportFileDto> reportFileDtos = new ArrayList<>();
            for (ReportFile reportFile : report.getReportFiles()) {
                ReportFileDto reportFileDto = reportFilesService.convertReportFileToReportFileDto(reportFile);
                reportFileDtos.add(reportFileDto);
            }

            // 轉換 Report -> ReportDto
            ReportDto reportDto = reportService.convertReportToReportDto(report);
            reportDto.setReportFileDtos(reportFileDtos);  // 設定專屬於該報告的附件
            ObjectMapper objectMapper = new ObjectMapper();
            reportDto.setJsonReportFileDtos(objectMapper.writeValueAsString(reportFileDtos));
            // 設定報告時間 & 執行日期
            if (report.getReportTime() != null) {
                reportDto.setReportTimeString(report.getReportTime().format(dateTimeFormatter));
            }
            if (report.getExecutionDate() != null) {
                reportDto.setExecutionDateString(simpleDateFormat.format(report.getExecutionDate()));
            }
            reportDtos.add(reportDto);
        }

        // 3. 設置回報清單
        incidentDto.setReportDtos(reportDtos);

        // 4. 設置最後回報時間
        if (incident.getLastReportTime() != null) {
            incidentDto.setLastReportTime(incident.getLastReportTime().format(dateTimeFormatter));
        }

        // 5. 設置報告日期
        incidentDto.setReportDate(simpleDateFormat.format(incident.getReportDate()));

        // 6. 設置最後回報者
        List<Report> reports = incident.getReports();
        String lastReporter = (!reports.isEmpty()) ? reports.get(reports.size() - 1).getReporter() : "";
        model.addAttribute("incidentDto", incidentDto);
        model.addAttribute("lastReporter", lastReporter);
        return "incident/incident_Detail";
    }

    //新增事件_回報
    @PostMapping("/incident/incident_inster_report")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> incident_inster_report(@RequestPart(value = "reportFiles", required = false) List<MultipartFile> reportFiles,
                                                                      @RequestPart("reportJson") String reportJson
    ) {

        Map<String, Object> response = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        Report report;
        try {
            // 解析 JSON 字符串為 Incident 物件
            report = objectMapper.readValue(reportJson, Report.class);
        } catch (JsonProcessingException e) {
            response.put("code", 400);
            response.put("message", "JSON 解析錯誤");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        // 檢查事件是否存在
        Incident existingIncident = incidentService.getIncident(report.getIncidentId());
        if (existingIncident == null) {
            response.put("code", 404);
            response.put("message", "事件不存在");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        existingIncident.setStatus(report.getProgress());
        existingIncident.setLastReportTime(LocalDateTime.now());
        report.setReportTime(LocalDateTime.now());
        try {
            reportService.insertReport(existingIncident, report, reportFiles);
            response.put("code", 200);
            response.put("message", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("code", 500);
            response.put("message", "伺服器錯誤，請稍後再試");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

    }

    //轉去更新事件頁
    @GetMapping("/incident/update_incident")
    public String into_update_incident(HttpServletRequest request, @RequestParam Integer incidentId, Model model) {
        Incident incident = incidentService.getIncident(incidentId);
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
            List<String> roles = jwtTokenProvider.getUserRolesFromToken(token);

            boolean isAdmin = roles.contains("ROLE_ADMIN");
            boolean isIncidentAdmin = roles.contains("ROLE_INCIDENT_ADMIN");
            System.out.println(!isAdmin);
            System.out.println(incident.getReportCount() != 0);
            if (incident != null && incident.getReportCount() != 0 && incident.getReportCount() > 0
                    && !(isAdmin || isIncidentAdmin)) {
                model.addAttribute("errorMessage", "此事件已有通報紀錄，無法編輯，如需編輯請使用管理權限登入");
                return "incident/incident_edit_forbidden";
            }
        }

        IncidentDto incidentDto = incidentService.convertIncidentToIncidentDto(incident);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        // 1. 轉換事件附件
        List<IncidentFileDto> incidentFileDtos = new ArrayList<>();
        for (IncidentFile incidentFile : incident.getIncidentFiles()) {
            IncidentFileDto incidentFileDto = incidentFilesService.convertIncidentFileToIncidentFileDto(incidentFile);
            incidentFileDto.setIncidentId(incident.getIncidentId());
            incidentFileDtos.add(incidentFileDto);
        }
        incidentDto.setIncidentFileDtos(incidentFileDtos);
        model.addAttribute("incidentDto", incidentDto);
        return "incident/update_incident";
    }

    //更新事件_回報
    @PutMapping("/incident/incident_update_report")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> incident_update_report(@RequestPart(value = "reportFiles", required = false) List<MultipartFile> reportFiles,
                                                                      @RequestPart("reportJson") String reportJson) {

        Map<String, Object> response = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        Report report;
        try {
            // 解析 JSON 字符串為 Incident 物件
            report = objectMapper.readValue(reportJson, Report.class);
        } catch (JsonProcessingException e) {
            response.put("code", 400);
            response.put("message", "JSON 解析錯誤");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        // 檢查事件是否存在
        Incident existingIncident = incidentService.getIncident(report.getIncidentId());
        if (existingIncident == null) {
            response.put("code", 404);
            response.put("message", "事件不存在");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        existingIncident.setStatus(report.getProgress());
        existingIncident.setLastReportTime(LocalDateTime.now());

        // 檢查回報是否存在
        Report existingReport = reportService.getReport(report.getReportId());
        if (existingReport == null) {
            response.put("code", 404);
            response.put("message", "事件不存在");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        existingReport.setReporter(report.getReporter());
        existingReport.setProgress(report.getProgress());
        existingReport.setExecutionDate(report.getExecutionDate());
        existingReport.setExecutor(report.getExecutor());
        existingReport.setContent(report.getContent());
        try {
            reportService.updateReport(existingIncident, existingReport, reportFiles);
            response.put("code", 200);
            response.put("message", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("code", 500);
            response.put("message", "伺服器錯誤，請稍後再試");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

        }

    }

    @PutMapping("/incident/incident_update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> incident_update_all(@RequestPart(value = "files", required = false) List<MultipartFile> files,
                                                                   @RequestPart("incidentJson") String incidentJson) {

        Map<String, Object> response = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        Incident incident;
        try {
            // 解析 JSON 字符串為 Incident 物件
            incident = objectMapper.readValue(incidentJson, Incident.class);
        } catch (JsonProcessingException e) {
            response.put("code", 400);
            response.put("message", "JSON 解析錯誤");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        // 檢查事件是否存在
        Incident existingIncident = incidentService.getIncident(incident.getIncidentId());
        if (existingIncident == null) {
            response.put("code", 404);
            response.put("message", "事件不存在");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        // 上傳檔案（若有）
        List<IncidentFile> uploadedFiles = new ArrayList<>();
        // 用來追蹤已儲存的臨時檔案
        List<File> tempFiles = new ArrayList<>();
        // 設定存放附件的本地資料夾
//        String uploadDir = "C:\\Users\\gagood72\\Desktop\\chuan\\incident_file\\";
        String uploadDirBase = "\\\\192.168.2.3\\群運共用夾\\※資訊部專用\\沈世泉\\incident_file\\";
        String eventFolderPath = uploadDirBase + "incident_" + existingIncident.getIncidentId();
        File dir = new File(eventFolderPath);
        // 嘗試存儲檔案
        if (files != null && !files.isEmpty()) {
            if (!dir.exists() && !dir.mkdirs()) {
                response.put("code", 500);
                response.put("message", "無法建立附件存放目錄");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            // 定義最大檔案大小 10MB (10485760 字節)
            long MAX_FILE_SIZE = 10 * 1024 * 1024;
            try {
                // 儲存檔案並創建檔案記錄
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        String originalFilename = file.getOriginalFilename();
                        if (originalFilename != null) {
                            // 儲存檔案到事件資料夾
                            String filePath = eventFolderPath + File.separator + originalFilename;
                            File storedFile = new File(filePath);
                            file.transferTo(storedFile);
                            tempFiles.add(storedFile);  // 將已儲存的檔案加入列表

                            // 創建 IncidentFiles 物件並設定檔案路徑
                            IncidentFile incidentFile = new IncidentFile();
                            incidentFile.setFileName(originalFilename);
                            incidentFile.setFilePath(filePath);
                            uploadedFiles.add(incidentFile);
                        }
                    }
                }
            } catch (IOException e) {
                response.put("code", 500);
                response.put("message", "檔案上傳失敗");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        }
        try {
            existingIncident.setBelongingType(incident.getBelongingType());
            existingIncident.setReporter(incident.getReporter());
            existingIncident.setTask(incident.getTask());
            existingIncident.setEventContent(incident.getEventContent());
            existingIncident.setReportCount(incident.getReports().size());
            existingIncident.setImportance(incident.getImportance());
            existingIncident.setDepartment(incident.getDepartment());
            existingIncident.setHandler(incident.getHandler());
            existingIncident.setStatus(incident.getStatus());
            existingIncident.setReportDate(incident.getReportDate());
            existingIncident.setEventPurpose(incident.getEventPurpose());
            // 若有新檔案則存入
            if (!uploadedFiles.isEmpty()) {
                existingIncident.getIncidentFiles().addAll(uploadedFiles);
            }
            incidentService.update_incident_and_files(existingIncident, existingIncident.getIncidentFiles());
            response.put("code", 200);
            response.put("message", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            for (File tempFile : tempFiles) {
                if (tempFile.exists()) {
                    tempFile.delete();  // 刪除已存檔案
                }
            }
            e.printStackTrace();
            response.put("code", 500);
            response.put("message", "伺服器錯誤，請稍後再試");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    //作廢事件
    @PutMapping("/incident/incident_invalid")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> incident_delete(@RequestParam Integer incidentId, HttpServletRequest request) {
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
            boolean isAdmin = roles.contains("ROLE_ADMIN");
            boolean isIncidentAdmin = roles.contains("ROLE_INCIDENT_ADMIN");
            // 取得事件
            Incident incident = incidentService.getIncident(incidentId);

            // 非管理員且不是建立者 → 不允許作廢
            if (!(isAdmin || isIncidentAdmin) && !incident.getCreator().equals(userName)) {
                response.put("code", 403);
                response.put("message", "只有事件建立者或管理員可以作廢事件");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            incidentService.invalidIncident(incident);
            response.put("code", 200);
            response.put("message", "Report invalid successfully.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("code", 500);
            response.put("message", "伺服器錯誤，請稍後再試");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    //搜尋過濾事件
    @PostMapping("/incident/filter_select")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> incident_filter_status(
            @RequestParam(defaultValue = "0") int page, // 頁數
            @RequestParam(defaultValue = "10") int size, // 每頁顯示多少筆資料
            @RequestParam String searchInput,
            @RequestParam String selectedDate,
            @RequestParam String selectedStatus,
            @RequestParam String selectImportance
    ) throws ParseException {
        System.out.println(selectedStatus);

        Pageable pageable = PageRequest.of(page, size);
        Map<String, Object> response = new HashMap<>();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        List<IncidentDto> incidentDtos = new ArrayList<>();
        Map<String, Object> data = new HashMap<>();
        // **將空字串轉為 null，讓 JPA 自動忽略條件**
        searchInput = (searchInput != null && !searchInput.trim().isEmpty()) ? searchInput.trim() : null;
        selectedStatus = (selectedStatus != null && !selectedStatus.trim().isEmpty()) ? selectedStatus.trim() : null;
        selectImportance = (selectImportance != null && !selectImportance.trim().isEmpty()) ? selectImportance.trim() : null;
        Date startDate = null;
        Date endDate = null;

        if (selectedDate != null && !selectedDate.trim().isEmpty() && selectedDate.contains(" - ")) {
            String[] dateParts = selectedDate.split(" - ");
            if (dateParts.length == 2) {
                startDate = simpleDateFormat.parse(convertToNewFormat(dateParts[0]));
                endDate = simpleDateFormat.parse(convertToNewFormat(dateParts[1]));
            }
        }
        try {

            if (selectedStatus == null && searchInput == null && startDate == null && endDate == null && selectImportance == null) {
                response.put("code", 400);
                response.put("message", "無符合條件的案件");
                return ResponseEntity.badRequest().body(response);
            }
            // 執行查詢
            Page<Incident> incidentPage = incidentService.findAllByStatusAndSearchInputAndDateRange(pageable, selectedStatus, selectImportance, searchInput, startDate, endDate);

            // 轉換 Incident 為 DTO
            for (Incident incident : incidentPage.getContent()) {
                IncidentDto incidentDto = incidentService.convertIncidentToIncidentDto(incident);
                incidentDto.setReportDate(simpleDateFormat.format(incident.getReportDate()));
                if (incident.getLastReportTime() != null) {
                    incidentDto.setLastReportTime(incident.getLastReportTime().format(dateTimeFormatter));
                } else {
                    incidentDto.setLastReportTime(""); // 避免 null 值
                }
                // 計算事件附件數量，先初始化為事件文件的數量
                int totalFiles = incident.getIncidentFiles() != null ? incident.getIncidentFiles().size() : 0;

                // 計算每個報告的附件數量
                totalFiles += incident.getReports().stream()
                        .mapToInt(report -> report.getReportFiles() != null ? report.getReportFiles().size() : 0)
                        .sum();

                // 設置附件數量
                incidentDto.setTotalFiles(totalFiles);
                incidentDtos.add(incidentDto);
            }
            System.out.println(incidentDtos.size());
            response.put("code", 200);
            response.put("message", "查詢成功");
            response.put("totalPages", incidentPage.getTotalPages()); // 總頁數
            response.put("totalElements", incidentPage.getTotalElements()); // 總筆數
            response.put("currentPage", page); // 當前頁數
            data.put("incidentDtos", incidentDtos);
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("code", 500);
            response.put("message", "伺服器錯誤，請稍後再試");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/incident/filter_recent")
    public ResponseEntity<Map<String, Object>> getRecentReports(@RequestParam(defaultValue = "0") int page, // 頁數
                                                                @RequestParam(defaultValue = "10") int size// 每頁顯示多少筆資料

    ) {

        Pageable pageable = PageRequest.of(page, size);
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        List<IncidentDto> incidentDtos = new ArrayList<>();
        try {
            // 執行查詢
            Page<Incident> incidentPage = incidentService.getRecentReports(pageable);
            // 轉換 Incident 為 DTO
            for (Incident incident : incidentPage.getContent()) {
                IncidentDto incidentDto = incidentService.convertIncidentToIncidentDto(incident);
                incidentDto.setReportDate(simpleDateFormat.format(incident.getReportDate()));
                if (incident.getLastReportTime() != null) {
                    incidentDto.setLastReportTime(incident.getLastReportTime().format(dateTimeFormatter));
                } else {
                    incidentDto.setLastReportTime(""); // 避免 null 值
                }
                // 計算事件附件數量，先初始化為事件文件的數量
                int totalFiles = incident.getIncidentFiles() != null ? incident.getIncidentFiles().size() : 0;

                // 計算每個報告的附件數量
                totalFiles += incident.getReports().stream()
                        .mapToInt(report -> report.getReportFiles() != null ? report.getReportFiles().size() : 0)
                        .sum();

                // 設置附件數量
                incidentDto.setTotalFiles(totalFiles);
                incidentDtos.add(incidentDto);
            }
            response.put("code", 200);
            response.put("message", "查詢成功");
            response.put("totalPages", incidentPage.getTotalPages()); // 總頁數
            response.put("totalElements", incidentPage.getTotalElements()); // 總筆數
            response.put("currentPage", page); // 當前頁數
            data.put("incidentDtos", incidentDtos);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("code", 500);
            response.put("message", "伺服器錯誤，請稍後再試");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

    }

    //刪除事件_附件
    @DeleteMapping("/incident/delete_file")
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
            boolean isAdmin = roles.contains("ROLE_ADMIN");
            IncidentFile incidentFile = incidentFilesService.getIncidentFile(fileId);
            // 只有上傳者本人或管理員可刪
            if (!isAdmin && !incidentFile.getIncident().getCreator().equals(userName)) {
                response.put("code", 403);
                response.put("message", "只有檔案上傳者或管理員可以刪除檔案");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            // 先刪除資料庫紀錄
            incidentFilesService.deleteFile(incidentFile);
            Path filePath = Paths.get(incidentFile.getFilePath());
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

    //刪除事件_回報的附件
    @DeleteMapping("/incident/delete_reportfile")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> incident_delete_Reportfile(@RequestParam String fileId, HttpServletRequest request) {

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
            boolean isAdmin = roles.contains("ROLE_ADMIN");
            ReportFile reportFile = reportFilesService.getReportFile(fileId);
            // 只有上傳者本人或管理員可刪
            if (!isAdmin && !reportFile.getReport().getReporter().equals(userName)) {
                response.put("code", 403);
                response.put("message", "只有檔案上傳者或管理員可以刪除檔案");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            // 刪除回報的附件
            reportFilesService.deleteFile(reportFile);
            Path filePath = Paths.get(reportFile.getFilePath());
            System.out.println(filePath.toString());
            Files.deleteIfExists(filePath);
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


    @GetMapping("/incident/status_counts")
    public ResponseEntity<Map<String, Object>> getStatusCounts() {
        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, Object> statusCounts = new HashMap<>();
            statusCounts.put("recent", incidentService.getRecentReportsCounts());
            statusCounts.put("unstarted", incidentService.getStatusCounts("未開始"));
            statusCounts.put("pending", incidentService.getStatusCounts("待處理"));
            statusCounts.put("inProgress", incidentService.getStatusCounts("處理中"));
            statusCounts.put("delayed", incidentService.getStatusCounts("延期"));
            statusCounts.put("awaitingSettlement", incidentService.getStatusCounts("待結案(待收款)"));

            response.put("code", 200);
            response.put("data", statusCounts);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace(); // 可以幫助日後除錯
            response.put("code", 500);
            response.put("message", "取得狀態統計時發生錯誤");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    //日期轉換方法
    private static String convertToNewFormat(String originalDate) {
        String[] parts = originalDate.split("年|月|日");
        if (parts.length == 3) {
            return parts[0] + "-" + String.format("%02d", Integer.parseInt(parts[1])) + "-" + String.format("%02d", Integer.parseInt(parts[2]));
        } else {
            return originalDate;
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


