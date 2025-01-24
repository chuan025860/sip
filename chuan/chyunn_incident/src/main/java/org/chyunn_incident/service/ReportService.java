package org.chyunn_incident.service;

import org.chyunn_incident.bean.Incident;
import org.chyunn_incident.bean.Report;
import org.chyunn_incident.dto.IncidentDto;
import org.chyunn_incident.dto.ReportDto;
import org.chyunn_incident.repository.IncidentRepository;
import org.chyunn_incident.repository.ReportRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ReportService {
    @Autowired
    ReportRepository reportRepository;
    @Autowired
    IncidentRepository incidentRepository;

    public Boolean insertReport(Incident incident, Report report) {
        report.setIncident(incident);
        List<Report> reports = new ArrayList<>();
        reports.add(report);
        incident.setReports(reports);
        incident.setReportCount(incident.getReportCount()+1);
        try {
            incidentRepository.save(incident);
            return true;
        } catch (Exception e) {
            return false;
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
}
