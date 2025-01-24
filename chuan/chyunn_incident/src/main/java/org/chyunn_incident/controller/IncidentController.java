package org.chyunn_incident.controller;

import org.chyunn_incident.bean.Incident;
import org.chyunn_incident.bean.Report;
import org.chyunn_incident.dto.IncidentDto;
import org.chyunn_incident.dto.ReportDto;
import org.chyunn_incident.service.IncidentService;
import org.chyunn_incident.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Controller
public class    IncidentController {
    @Autowired
    IncidentService incidentService;
    @Autowired
    ReportService reportService;

    //轉去事件追蹤頁
    @GetMapping("/incident/select_incident")
    public String into_select_incident() {
        return "/incident/select_incident";
    }

    //查找所有事件
    @PostMapping("/incident/select_incident")
    @ResponseBody
    public List<IncidentDto> select_incident() {
        List<Incident> incidents = incidentService.findAll();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        List<IncidentDto> incidentDtos = new ArrayList<>();
        for (Incident incident : incidents) {
            IncidentDto incidentDto = incidentService.convertIncidentToIncidentDto(incident);
            incidentDto.setLastReportTime(incident.getLastReportTime().format(dateTimeFormatter));
            incidentDto.setReportDate(simpleDateFormat.format(incident.getReportDate()));
            incidentDto.setReports(null);
            incidentDtos.add(incidentDto);
        }
        return incidentDtos;
    }

    //轉去事件新增頁
    @GetMapping("/incident/inster_incident")
    public String into_inster_incident() {
        return "/incident/inster_incident";
    }

    //事件新增
    @PostMapping("/incident/inster_incident")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> inster_incident(@RequestBody Incident incident) {
        Map<String, Object> response = new HashMap<>();
        incident.setStatus("待處理");
        incident.setReportCount(0);
        incident.setLastReportTime(LocalDateTime.now());
        Incident newIncident = incidentService.insert(incident);
        if (newIncident != null) {
            Map<String, Object> data = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            data.put("incident", newIncident);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } else {
            response.put("code", 401);
            response.put("message", "資料錯誤");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    //轉去事件明細
    @GetMapping("/incident/incident_Detail")
    public String incident_Detail(@RequestParam String incidentId, Model model) {
        Incident incident = incidentService.getIncident(incidentId);
        IncidentDto incidentDto = incidentService.convertIncidentToIncidentDto(incident);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        incidentDto.setLastReportTime(incident.getLastReportTime().format(dateTimeFormatter));
        incidentDto.setReportDate(simpleDateFormat.format(incident.getReportDate()));
        // 設置報告詳細信息
        List<Report> reports = incident.getReports();
        String reporter = "";

        if (reports != null && !reports.isEmpty()) {
            // 設置報告時間和執行日期
            for (Report report : reports) {
                if (report.getReportTime() != null) {
                    report.setReportTimeString(report.getReportTime().format(dateTimeFormatter));
                }
                if (report.getExecutionDate() != null) {
                    report.setExecutionDateString(simpleDateFormat.format(report.getExecutionDate()));
                }
            }
            // 取得最後一條報告的報告者
            reporter = reports.get(reports.size() - 1).getReporter();
        }
        System.out.println(reporter);
        model.addAttribute("incidentDto", incidentDto);
        model.addAttribute("lastReporter", reporter);
        return "/incident/incident_Detail";
    }

    //新增事件_回報
    @PostMapping("/incident/incident_inster_report")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> incident_inster_report(@RequestBody Report report) {
        Map<String, Object> response = new HashMap<>();
        String incidentId = report.getIncidentId();
        Incident incident = incidentService.getIncident(incidentId);
        incident.setStatus(report.getProgress());
        incident.setLastReportTime(LocalDateTime.now());
        report.setReportTime(LocalDateTime.now());
        if (reportService.insertReport(incident, report)) {
            response.put("code", 200);
            response.put("message", "success");
            return ResponseEntity.ok(response);
        } else {
            response.put("code", 401);
            response.put("message", "新增失敗");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    //轉去更新事件頁
    @GetMapping("/incident/update_incident")
    public String into_update_incident(@RequestParam String incidentId, Model model) {
        Incident incident = incidentService.getIncident(incidentId);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        IncidentDto incidentDto = incidentService.convertIncidentToIncidentDto(incident);
        incidentDto.setLastReportTime(incident.getLastReportTime().format(dateTimeFormatter));
        incidentDto.setReportDate(simpleDateFormat.format(incident.getReportDate()));
        for (Report report : incident.getReports()) {
            report.setReportTimeString(report.getReportTime().format(dateTimeFormatter));
            report.setExecutionDateString(simpleDateFormat.format(report.getExecutionDate()));
        }
        model.addAttribute("incidentDto", incidentDto);
        return "/incident/update_incident";
    }

    //刪除事件_回報
    @DeleteMapping("/incident/incident_delete_report")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> incident_delete_report(@RequestParam int reportId, @RequestParam String incidentId) {
        System.out.println(reportId + "---------------------------");
        Map<String, Object> response = new HashMap<>();
        // 獲取要刪除的報告
        Report reportToDelete = reportService.getReport(reportId);
        try {
            // 刪除報告
            if (reportService.deleteReport(reportToDelete)) {
                Incident incident = incidentService.getIncident(incidentId);
                List<Report> reports = incident.getReports();
                if (!reports.isEmpty()) {
                    incident.setStatus(reports.get(reports.size() - 1).getProgress());
                    incident.setLastReportTime(reports.get(reports.size() - 1).getReportTime());
                    incident.setReportCount(incident.getReportCount() - 1);
                    incidentService.insert(incident);
                }
                response.put("code", 200);
                response.put("message", "Report deleted successfully.");
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 401);
                response.put("message", "Failed to delete report.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "An error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    //更新事件_回報
    @PutMapping("/incident/incident_update_report")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> incident_update_report(@RequestBody Report report) {
        Map<String, Object> response = new HashMap<>();
        String incidentId = report.getIncidentId();
        Incident incident = incidentService.getIncident(incidentId);
        List<Report> reports = incident.getReports();
        // 檢查報告列表是否存在
        if (reports == null || reports.isEmpty()) {
            response.put("message", "No reports found for this incident.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        Report newReport = reportService.getReport(report.getReportId());
        newReport.setReporter(report.getReporter());
        newReport.setProgress(report.getProgress());
        newReport.setExecutionDate(report.getExecutionDate());
        newReport.setExecutor(report.getExecutor());
        newReport.setContent(report.getContent());

        Report lastReport = reports.get(reports.size() - 1);
        if (lastReport.getReportId() == report.getReportId()) {
            incident.setStatus(report.getProgress());
        }

        if (reportService.insertReport(incident, newReport)) {
            System.out.println("-------------------------------------");
            response.put("code", 200);
            response.put("message", "success");
            return ResponseEntity.ok(response);
        } else {
            response.put("code", 401);
            response.put("message", "新增失敗");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping("/incident/incident_update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> incident_update_all(@RequestBody Incident incident) {
        Map<String, Object> response = new HashMap<>();
        System.out.println(incident.toString());
        Incident newIncident = incidentService.getIncident(incident.getIncidentId());
        if (newIncident == null) {
            response.put("code", 401);
            response.put("message", "No reports found for this incident.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } else {
            newIncident.setBelongingType(incident.getBelongingType());
            newIncident.setReporter(incident.getReporter());
            newIncident.setTask(incident.getTask());
            newIncident.setEventContent(incident.getEventContent());
            newIncident.setReportCount(incident.getReports().size());
            newIncident.setImportance(incident.getImportance());
            newIncident.setDepartment(incident.getDepartment());
            newIncident.setHandler(incident.getHandler());
            newIncident.setStatus(incident.getStatus());
            newIncident.setReportDate(incident.getReportDate());
            incidentService.insert(newIncident);
            response.put("code", 200);
            response.put("message", "success");
            return ResponseEntity.ok(response);
        }
    }

    //作廢事件
    @PutMapping("/incident/incident_invalid")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> incident_delete(@RequestParam String incidentId) {

        Map<String, Object> response = new HashMap<>();
        if ( incidentService.invalidIncident(incidentService.getIncident(incidentId))) {
            response.put("code", 200);
            response.put("message", "Report invalid successfully.");
            return ResponseEntity.ok(response);
        }else {
            response.put("code", 401);
            response.put("message", "Failed to invalid report.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    //搜尋過濾事件
    @PostMapping("/incident/filter_status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> incident_filter_status(@RequestParam String selectedStatus) {
        Map<String, Object> response = new HashMap<>();
        List<Incident> incidents = incidentService.findAllByStatus(selectedStatus);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        List<IncidentDto> incidentDtos = new ArrayList<>();
        for (Incident incident : incidents) {
            IncidentDto incidentDto = incidentService.convertIncidentToIncidentDto(incident);
            incidentDto.setLastReportTime(incident.getLastReportTime().format(dateTimeFormatter));
            incidentDto.setReportDate(simpleDateFormat.format(incident.getReportDate()));
            incidentDto.setReports(null);
            incidentDtos.add(incidentDto);
        }
        if (incidentDtos!=null ) {
            Map<String, Object> data = new HashMap<>();
            response.put("code", 200);
            data.put("incidentDtos", incidentDtos);
            response.put("data", data);
            response.put("message", "Report invalid successfully.");
            return ResponseEntity.ok(response);
        }else {
            response.put("code", 401);
            response.put("message", "Failed to select report.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

    }


}
