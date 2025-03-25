package org.chyunn_web.service;

import org.chyunn_web.bean.Incident;
import org.chyunn_web.bean.Inventory_Equipment;
import org.chyunn_web.dto.IncidentDto;
import org.chyunn_web.dto.Inventory_EquipmentDto;
import org.chyunn_web.repository.AssetRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AssetService {
@Autowired  AssetRepository assetRepository;
    public Page<Inventory_Equipment> findAll(Pageable pageable) {
        return assetRepository.findAll(pageable);
    }

    private ModelMapper modelMapper = new ModelMapper();

    public Inventory_EquipmentDto convertInventory_EquipmentDto(Inventory_Equipment inventoryEquipment) {
        // 自動映射
        return modelMapper.map(inventoryEquipment, Inventory_EquipmentDto.class);
    }
    public Inventory_Equipment  getPropertyDetails(String id) {
        assetRepository.findById(id);
        Optional<Inventory_Equipment> optional =   assetRepository.findById(id);
        if (optional.isPresent()) {
            Inventory_Equipment inventoryEquipment = optional.get();
            return inventoryEquipment;
        } else {
            return null;
        }
    }

}
