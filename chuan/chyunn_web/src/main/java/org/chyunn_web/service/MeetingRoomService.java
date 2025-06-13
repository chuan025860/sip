package org.chyunn_web.service;

import org.chyunn_web.bean.Resource.MeetingRoom;
import org.chyunn_web.repository.MeetingRoomRepository;
import org.chyunn_web.repository.ResourceBorrowRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MeetingRoomService {
    @Autowired
    private MeetingRoomRepository meetingRoomRepository;
    @Autowired
    ResourceBorrowRequestRepository resourceBorrowRequestRepository;
    public List<MeetingRoom> getAvailableRooms(LocalDateTime startTime, LocalDateTime endTime){
        return meetingRoomRepository.findAvailableRooms(startTime, endTime);
    }
    public  MeetingRoom findMeetingRoomById(int id){
        Optional<MeetingRoom> optional = meetingRoomRepository.findById(id);
        if(optional.isPresent()){
            return optional.get();
        }else {
            return null;
        }
    }
    public boolean isAvailable(Integer roomId, LocalDateTime start, LocalDateTime end, Integer excludeRequestId) {
        return resourceBorrowRequestRepository.findConflictsForRoom(roomId, start, end, excludeRequestId).isEmpty();
    }
}
