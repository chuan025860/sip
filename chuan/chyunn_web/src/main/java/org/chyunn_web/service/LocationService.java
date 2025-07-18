package org.chyunn_web.service;

import org.chyunn_web.bean.Asset.FixedAssetLocation;
import org.chyunn_web.bean.Asset.Location;
import org.chyunn_web.repository.FixedAssetLocationRepository;
import org.chyunn_web.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationService {
    @Autowired
    private LocationRepository locationRepository;
    @Autowired
    private FixedAssetLocationRepository fixedAssetLocationRepository;
    public List<Location> findAll() {
        return locationRepository.findAll();
    }
    public List<FixedAssetLocation> findAllFixedAssetLocation() {return fixedAssetLocationRepository.findAll();}
}
