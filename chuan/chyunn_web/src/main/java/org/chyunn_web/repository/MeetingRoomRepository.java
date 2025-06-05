package org.chyunn_web.repository;

import org.chyunn_web.bean.Asset.General_Catalog;
import org.chyunn_web.bean.Resource.MeetingRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MeetingRoomRepository extends JpaRepository<MeetingRoom, Integer> {
    @Query("""
        SELECT m FROM MeetingRoom m
        WHERE m.id NOT IN (
            SELECT r.meetingRoom.id FROM ResourceBorrowRequest r
            WHERE r.category = 'ROOM'
              AND (
                :startTime < r.endTime AND :endTime > r.startTime
              )
        )
    """)
    List<MeetingRoom> findAvailableRooms(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
