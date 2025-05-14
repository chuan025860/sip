package org.chyunn_web.service;

import org.chyunn_web.bean.Location;
import org.chyunn_web.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationService {
    @Autowired
    private LocationRepository locationRepository;
    public List<Location> findAll() {
        return locationRepository.findAll();
    }
}
