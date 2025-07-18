package org.chyunn_web.service;

import org.chyunn_web.bean.Asset.AssetHistory;
import org.chyunn_web.bean.Asset.FixedAssetHistory;
import org.chyunn_web.dto.AssetHistoryDto;
import org.chyunn_web.dto.FixedAssetHistoryDto;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class AssetHistoryService {

    private ModelMapper modelMapper = new ModelMapper();
    public AssetHistoryDto convert_AssetHistoryDto(AssetHistory assetHistory) {
        // 自動映射
        return modelMapper.map(assetHistory, AssetHistoryDto.class);
    }
    public FixedAssetHistoryDto convert_FixedAssetHistory(FixedAssetHistory fixedAssetHistory) {
        // 自動映射
        return modelMapper.map(fixedAssetHistory, FixedAssetHistoryDto.class);
    }
}
