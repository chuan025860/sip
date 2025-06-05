package org.chyunn_web.repository;

import org.chyunn_web.bean.Resource.MeetingRoom;
import org.chyunn_web.bean.Resource.Vehicle;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {
    @Query("""
        SELECT v FROM Vehicle v
        WHERE v.id NOT IN (
            SELECT r.vehicle.id FROM ResourceBorrowRequest r
            WHERE r.category = 'VEHICLE'
              AND (
                :startTime < r.endTime AND :endTime > r.startTime
              )
        )
    """)
    List<Vehicle> getAvailableVehicles(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
