package org.chyunn_web.service;

import jakarta.transaction.Transactional;
import org.chyunn_web.bean.incident.Incident;
import org.chyunn_web.bean.incident.IncidentFile;
import org.chyunn_web.dto.IncidentDto;
import org.chyunn_web.repository.IncidentRepository;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class IncidentService {
    @Autowired
    IncidentRepository incidentRepository;


    //做兩次save  需要開交易  避免發生錯誤沒有回滾
    @Transactional
    public Incident insert_incident_and_files(Incident incident, List<MultipartFile> files) throws IOException {
        // 儲存事件資料
        Incident newIncident = incidentRepository.save(incident);  // 插入事件並獲取 ID
        if (files == null) {
            //無附件直接回傳
            return newIncident;
        }
        // 根據事件 ID 創建資料夾
//        String uploadDirBase = "C:\\Users\\gagood72\\Desktop\\chuan\\incident_file\\";
        String uploadDirBase = "\\\\192.168.2.3\\群運共用夾\\※資訊部專用\\沈世泉\\incident_file\\";
        String eventFolderPath = uploadDirBase + "incident_" + newIncident.getIncidentId();
        File eventDir = new File(eventFolderPath);
        if (!eventDir.exists() && !eventDir.mkdirs()) {
            throw new IOException("無法建立事件資料夾");
        }
        List<IncidentFile> incidentFiles = new ArrayList<>();
        List<File> tempFiles = new ArrayList<>(); // 用來追蹤已儲存的臨時檔案
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
                    IncidentFile incidentFile = new IncidentFile();
                    incidentFile.setFileName(originalFilename);
                    incidentFile.setFilePath(filePath);
                    incidentFiles.add(incidentFile);
                }
            }
        }
        try {
            for (IncidentFile file : incidentFiles) {
                file.setIncident(newIncident);  // Incident 關聯
            }
            newIncident.setIncidentFiles(incidentFiles);
            return incidentRepository.save(newIncident);
        } catch (Exception e) {
            e.printStackTrace();
            // **刪除整個資料夾（包含所有檔案）**
            deleteFolder(eventDir);
            throw new RuntimeException(e); // 重新拋出異常
        }
    }

    public Incident update_incident_and_files(Incident incident, List<IncidentFile> incidentFiles) {
        try {
            if (incidentFiles != null && incidentFiles.size() > 0) {
                for (IncidentFile file : incidentFiles) {
                    file.setIncident(incident);  // Incident 關聯
                }
                incident.setIncidentFiles(incidentFiles);
                return incidentRepository.save(incident);
            } else {
                return incidentRepository.save(incident);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e); // 重新拋出異常
        }
    }

    public Incident getIncident(Integer incidentId) {
        Optional<Incident> optional = incidentRepository.findById(incidentId);
        if (optional.isPresent()) {
            Incident incident = optional.get();
            return incident;
        } else {
            return null;
        }
    }

    public Page<Incident> findAll(Pageable pageable) {
        return incidentRepository.findAllByReportDateDescAndInvalidFalse(pageable);
    }

    public void invalidIncident(Incident incident) {
        try {
            incident.setStatus("已作廢");
            incident.setInvalid(true);
            incidentRepository.save(incident);
        } catch (Exception e) {
                throw new RuntimeException(e); // 重新拋出異常
        }
    }


    private ModelMapper modelMapper = new ModelMapper();

    public IncidentDto convertIncidentToIncidentDto(Incident incident) {
        // 自動映射
        return modelMapper.map(incident, IncidentDto.class);
    }

    public Integer getStatusCounts(String status) {
      return   incidentRepository.getStatusCounts(status);
    }

    public Page<Incident> findAllByStatusAndSearchInputAndDateRange(Pageable pageable,String status,String importance, String searchInput, Date startDate, Date endDate) {
        return incidentRepository.findAllByStatusAndSearchInputAndDateRange(pageable,status,importance, searchInput, startDate, endDate);
    }
    public Integer getRecentReportsCounts() {
        LocalDateTime tenDaysAgo = LocalDateTime.now().minusDays(10).truncatedTo(ChronoUnit.SECONDS);
        return  incidentRepository.getRecentReportsCounts(tenDaysAgo);
    }
    public Page<Incident> getRecentReports(Pageable pageable) {
        LocalDateTime tenDaysAgo = LocalDateTime.now().minusDays(10).truncatedTo(ChronoUnit.SECONDS);
        return  incidentRepository.getRecentReports(tenDaysAgo,pageable);
    }
    public Integer findMaxIncidentId() {
        return incidentRepository.findMaxIncidentId();

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
