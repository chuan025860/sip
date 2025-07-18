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
                ORDER BY CASE m.name
                        WHEN '群運營運中心一樓大會議室(103)' THEN 1
                        WHEN '群運營運中心一樓小會議室(116)' THEN 2
                        WHEN '群運營運中心二樓洽談室(202)' THEN 3
                        WHEN '群運營運中心二樓中會議室(218)' THEN 4
                        WHEN '群運安明一樓會議室' THEN 5
                         WHEN '群運營運中心二樓交誼廳' THEN 6
                        ELSE 999
                    END
            """)
    List<MeetingRoom> findAvailableRooms(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("""
    SELECT m FROM MeetingRoom m
    ORDER BY CASE m.name
        WHEN '群運營運中心一樓大會議室(103)' THEN 1
        WHEN '群運營運中心一樓小會議室(116)' THEN 2
        WHEN '群運營運中心二樓洽談室(202)' THEN 3
        WHEN '群運營運中心二樓中會議室(218)' THEN 4
        WHEN '群運安明一樓會議室' THEN 5
        WHEN '群運營運中心二樓交誼廳' THEN 6
        ELSE 999
    END
""")
    List<MeetingRoom> findAllOrderByCustomName();

}
