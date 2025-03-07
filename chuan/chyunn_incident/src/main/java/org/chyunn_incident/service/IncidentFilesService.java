package org.chyunn_incident.service;

import org.chyunn_incident.bean.Incident;
import org.chyunn_incident.bean.IncidentFile;
import org.chyunn_incident.dto.IncidentDto;
import org.chyunn_incident.dto.IncidentFileDto;
import org.chyunn_incident.repository.IncidentFilesRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class IncidentFilesService {
    @Autowired
    IncidentFilesRepository incidentFilesRepository;

    public void deleteFile(IncidentFile incidentFile) {
        try {
            incidentFilesRepository.delete(incidentFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public IncidentFile getIncidentFile(String imageId) {
        Optional<IncidentFile> optional = incidentFilesRepository.findById(imageId);
        if (optional.isPresent()) {
            IncidentFile incidentFile = optional.get();
            return incidentFile;
        } else {
            return null;
        }
    }

    private ModelMapper modelMapper = new ModelMapper();

    public IncidentFileDto convertIncidentFileToIncidentFileDto(IncidentFile incidentFiles) {
        // 自動映射
        return modelMapper.map(incidentFiles, IncidentFileDto.class);
    }
}
