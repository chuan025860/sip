package org.chyunn_web.service;

import org.chyunn_web.bean.AssetHistory;
import org.chyunn_web.bean.Inventory_Equipment;
import org.chyunn_web.dto.AssetHistoryDto;
import org.chyunn_web.dto.Inventory_EquipmentDto;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class AssetHistoryService {

    private ModelMapper modelMapper = new ModelMapper();
    public AssetHistoryDto convert_AssetHistoryDto(AssetHistory assetHistory) {
        // 自動映射
        return modelMapper.map(assetHistory, AssetHistoryDto.class);
    }
}
