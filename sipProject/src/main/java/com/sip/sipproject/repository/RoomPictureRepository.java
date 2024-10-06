package com.sip.sipproject.repository;

import com.sip.sipproject.bean.Room;
import com.sip.sipproject.bean.RoomPicture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface RoomPictureRepository extends JpaRepository<RoomPicture,Integer> {

    @Query("SELECT COUNT(r) FROM RoomPicture r WHERE r.room = :room")
    int countByProductID(Room room);

    @Query(value = "from RoomPicture r WHERE r.room= :room")
    List<RoomPicture> findPictureByRoom(@Param(value = "room") Room room);
}
