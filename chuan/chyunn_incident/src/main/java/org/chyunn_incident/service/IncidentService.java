package org.chyunn_incident.service;

import org.chyunn_incident.bean.Incident;
import org.chyunn_incident.bean.Report;
import org.chyunn_incident.dto.IncidentDto;
import org.chyunn_incident.repository.IncidentRepository;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class IncidentService {
    @Autowired
    IncidentRepository incidentRepository;

    public Incident insert(Incident incident) {
        try {
            return incidentRepository.save(incident);

        } catch (Exception e) {
            e.printStackTrace(); // 可選：處理異常並記錄錯誤
            return null;
        }
    }

    public Incident insert_incidentAndReport(Incident incident, List<Report> reports) {
        try {
            incident.setReports(reports);
            for (Report report : reports) {
                report.setIncident(incident);
            }
            return incidentRepository.save(incident);

        } catch (Exception e) {
            e.printStackTrace(); // 可選：處理異常並記錄錯誤
            return null;
        }
    }

    public Incident getIncident(String incidentId) {
        Optional<Incident> optional = incidentRepository.findById(incidentId);
        if (optional.isPresent()) {
            Incident incident = optional.get();
            return incident;
        } else {
            return null;
        }
    }

    public List<Incident> findAll() {
        return incidentRepository.findAllByReportDateDescAndInvalidFalse();
    }

    public Boolean invalidIncident(Incident incident) {
        try {
            incident.setStatus("已作廢");
            incident.setInvalid(true);
            incidentRepository.save(incident);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    private ModelMapper modelMapper = new ModelMapper();

    public IncidentDto convertIncidentToIncidentDto(Incident incident) {
        // 自動映射
        return modelMapper.map(incident, IncidentDto.class);
    }

    public List<Incident> findAllByStatus(String status) {
        if (status.equals("已作廢")){
            return incidentRepository.findAllByInvalid();
        }else {
            return incidentRepository.findAllByStatus(status);
        }

    }
}
