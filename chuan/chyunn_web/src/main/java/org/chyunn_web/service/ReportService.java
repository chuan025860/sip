package org.chyunn_web.service;

import org.chyunn_web.bean.Incident;
import org.chyunn_web.bean.Report;
import org.chyunn_web.bean.ReportFile;
import org.chyunn_web.dto.ReportDto;
import org.chyunn_web.repository.IncidentRepository;
import org.chyunn_web.repository.ReportRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class ReportService {
    @Autowired
    ReportRepository reportRepository;
    @Autowired
    IncidentRepository incidentRepository;

    public void         insertReport(Incident incident, Report report, List<MultipartFile> files) throws IOException {
        Integer reportCount = incident.getReportCount() + 1;
        // 根據事件 ID 的資料夾
//        String uploadDirBase = "C:\\Users\\gagood72\\Desktop\\chuan\\incident_file\\";
        String uploadDirBase = "\\\\192.168.2.3\\群運共用夾\\※資訊部專用\\沈世泉\\incident_file\\";
        String eventFolderPath = uploadDirBase + "incident_" + incident.getIncidentId() + "\\report\\" + reportCount;
        File eventDir = new File(eventFolderPath);
        if (!eventDir.exists() && !eventDir.mkdirs()) {
            throw new IOException("無法建立事件資料夾");
        }
        try {
            List<ReportFile> reportFiles = new ArrayList<>();
            List<File> tempFiles = new ArrayList<>(); // 用來追蹤已儲存的臨時檔案
            if (files != null) {
                // 儲存檔案並創建檔案記錄
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        String originalFilename = file.getOriginalFilename();
                        if (originalFilename != null) {
                            // 儲存檔案到事件資料夾
                            String filePath = eventFolderPath + File.separator + originalFilename;
                            File storedFile = new File(filePath);
                            file.transferTo(new File(filePath));
                            tempFiles.add(storedFile);  // 將已儲存的檔案加入列表
                            // 創建 IncidentFiles 物件並設定檔案路徑
                            ReportFile reportFile = new ReportFile();
                            reportFile.setFileName(originalFilename);
                            reportFile.setFilePath(filePath);
                            reportFile.setReport(report);
                            reportFiles.add(reportFile);
                        }
                    }
                }
                //進行關聯
                report.setIncident(incident);
                report.setReportFiles(reportFiles);
                List<Report> reports = new ArrayList<>();
                reports.add(report);
                incident.setReports(reports);
                incident.setReportCount(reportCount);
                incidentRepository.save(incident);
            } else {
                //進行關聯
                report.setIncident(incident);
                List<Report> reports = new ArrayList<>();
                reports.add(report);
                incident.setReports(reports);
                incident.setReportCount(reportCount);
                incidentRepository.save(incident);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // **刪除整個資料夾（包含所有檔案）**
            deleteFolder(eventDir);
            throw new RuntimeException("儲存失敗", e); // 重新拋出異常
        }
    }

    public void updateReport(Incident incident, Report report, List<MultipartFile> files) throws IOException {
        // 根據事件 ID 的資料夾
//        String uploadDirBase = "C:\\Users\\gagood72\\Desktop\\chuan\\incident_file\\";
        String uploadDirBase = "\\\\192.168.2.3\\群運共用夾\\※資訊部專用\\沈世泉\\incident_file\\";
        String eventFolderPath = uploadDirBase + "incident_" + incident.getIncidentId() + "\\report\\" + incident.getReportCount();
        File eventDir = new File(eventFolderPath);
        if (!eventDir.exists() && !eventDir.mkdirs()) {
            throw new IOException("無法建立事件資料夾");
        }
        List<File> tempFiles = new ArrayList<>(); // 用來追蹤已儲存的臨時檔案
        try {
            List<ReportFile> reportFiles = report.getReportFiles();
            if (files != null) {
                // 儲存檔案並創建檔案記錄
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        String originalFilename = file.getOriginalFilename();
                        if (originalFilename != null) {
                            // 檢查是否已存在同名的檔案紀錄
                            boolean fileExistsInDB = reportFiles.stream()
                                    .anyMatch(existingFile -> originalFilename.equals(existingFile.getFileName()));

                            // 儲存檔案到實體資料夾（覆蓋也沒關係）
                            String filePath = eventFolderPath + File.separator + originalFilename;
                            File storedFile = new File(filePath);
                            file.transferTo(storedFile); // 直接覆蓋
                            tempFiles.add(storedFile);

                            // 如果不存在於資料庫，就新增 ReportFile 記錄
                            if (!fileExistsInDB) {
                                ReportFile reportFile = new ReportFile();
                                reportFile.setFileName(originalFilename);
                                reportFile.setFilePath(filePath);
                                reportFile.setReport(report);
                                reportFiles.add(reportFile);
                            }
                        }
                    }
                }
                //進行關聯
                report.setIncident(incident);
                report.setReportFiles(reportFiles);
                List<Report> reports = new ArrayList<>();
                reports.add(report);
                incident.setReports(reports);
                incidentRepository.save(incident);
            } else {
                List<Report> reports = new ArrayList<>();
                reports.add(report);
                incident.setReports(reports);
                incidentRepository.save(incident);
            }
        } catch (Exception e) {
            for (File tempFile : tempFiles) {
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }
            throw new RuntimeException("儲存失敗", e);
        }
    }

    public Report getReport(Integer reportId) {
        Optional<Report> optional = reportRepository.findById(reportId);
        if (optional.isPresent()) {
            return optional.get();
        } else {
            return null;
        }
    }

    public Boolean deleteReport(Report report) {
        try {
            reportRepository.delete(report);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private ModelMapper modelMapper = new ModelMapper();

    public ReportDto convertReportToReportDto(Report report) {
        // 自動映射
        return modelMapper.map(report, ReportDto.class);
    }


    //刪除整個資料夾及其內容
    public void deleteFolder(File folder) {
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete(); // 刪除資料夾內所有檔案
                }
            }
            folder.delete(); // 最後刪除資料夾本身
        }
    }
}
