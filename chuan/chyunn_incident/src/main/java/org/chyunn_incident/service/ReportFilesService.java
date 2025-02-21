package org.chyunn_incident.service;

import org.chyunn_incident.bean.IncidentFile;
import org.chyunn_incident.bean.Report;
import org.chyunn_incident.bean.ReportFile;
import org.chyunn_incident.dto.ReportFileDto;
import org.chyunn_incident.repository.ReportFilesRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ReportFilesService {
    @Autowired
    ReportFilesRepository reportFilesRepository;

    public void deleteFile(ReportFile reportFile) {
        try {
            reportFilesRepository.delete(reportFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ReportFile getReportFile(String imageId) {
        Optional<ReportFile> optional = reportFilesRepository.findById(imageId);
        if (optional.isPresent()) {
            ReportFile reportFile = optional.get();
            return reportFile;
        } else {
            return null;
        }
    }
    private ModelMapper modelMapper = new ModelMapper();

    public ReportFileDto convertReportFileToReportFileDto(ReportFile reportFile) {
        // 自動映射
        return modelMapper.map(reportFile, ReportFileDto.class);
    }
}
