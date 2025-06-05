package org.chyunn_web.repository;

import org.chyunn_web.bean.Resource.ResourceBorrowRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ResourceBorrowRequestRepository extends JpaRepository<ResourceBorrowRequest, Integer> {
    @Query("SELECT r FROM ResourceBorrowRequest r WHERE r.startTime >= :start AND r.startTime <= :end ORDER BY r.startTime ASC ")
    Page<ResourceBorrowRequest> findByStartTimeBetween(
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    @Query("""
    SELECT r FROM ResourceBorrowRequest r
    WHERE r.category = 'ROOM'
      AND r.meetingRoom.id = :roomId
      AND (:excludeId IS NULL OR r.id != :excludeId)
      AND (:start < r.endTime AND :end > r.startTime)
""")
    List<ResourceBorrowRequest> findConflictsForRoom(
            @Param("roomId") Integer roomId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("excludeId") Integer excludeId
    );

    @Query("""
    SELECT r FROM ResourceBorrowRequest r
    WHERE r.category = 'VEHICLE'
      AND r.vehicle.id = :vehicleId
      AND (:excludeId IS NULL OR r.id != :excludeId)
      AND (:start < r.endTime AND :end > r.startTime)
""")
    List<ResourceBorrowRequest> findConflictsForVehicle(
            @Param("vehicleId") Integer vehicleId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("excludeId") Integer excludeId
    );
}
