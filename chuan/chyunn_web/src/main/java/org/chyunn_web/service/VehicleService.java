package org.chyunn_web.service;

import org.chyunn_web.bean.Resource.MeetingRoom;
import org.chyunn_web.bean.Resource.Vehicle;
import org.chyunn_web.repository.ResourceBorrowRequestRepository;
import org.chyunn_web.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VehicleService {
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private ResourceBorrowRequestRepository resourceBorrowRequestRepository;
    public List<Vehicle> getAvailableVehicles(LocalDateTime startTime, LocalDateTime endTime){
        return vehicleRepository.getAvailableVehicles(startTime, endTime);
    }
    public Vehicle findVehicleById(String vehicleId){
        Optional<Vehicle> optional=vehicleRepository.findById(Integer.valueOf(vehicleId));
        if(optional.isPresent()){
            return optional.get();
        }else {
            return null;
        }
    }
    public List<Vehicle> findAll(){
        return vehicleRepository.findAll();
    }
    public void insertVehicle(Vehicle vehicle){
        vehicleRepository.save(vehicle);
    }
    public void deleteVehicle(Vehicle vehicle){
        vehicleRepository.delete(vehicle);
    }
    public boolean isAvailable(Integer vehicleId, LocalDateTime start, LocalDateTime end, Integer excludeRequestId) {
        return resourceBorrowRequestRepository.findConflictsForVehicle(vehicleId, start, end, excludeRequestId).isEmpty();
    }
}
