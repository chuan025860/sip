package org.chyunn_web.service;

import org.chyunn_web.bean.incident.IncidentFile;
import org.chyunn_web.dto.IncidentFileDto;
import org.chyunn_web.repository.IncidentFilesRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
