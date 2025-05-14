package org.chyunn_web.service;

import org.chyunn_web.bean.AssetHistory;
import org.chyunn_web.bean.AssetHistory_All;
import org.chyunn_web.dto.AssetHistoryDto;
import org.chyunn_web.dto.AssetHistoryDto_All;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class AssetHistory_ALL_Service {
    private ModelMapper modelMapper = new ModelMapper();
    public AssetHistoryDto_All convert_AssetHistoryDto(AssetHistory_All assetHistory) {
        // 自動映射
        return modelMapper.map(assetHistory, AssetHistoryDto_All.class);
    }
}
