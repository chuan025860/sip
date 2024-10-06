package com.sip.sipproject.service;

import com.sip.sipproject.bean.Room;
import com.sip.sipproject.bean.RoomPicture;
import com.sip.sipproject.repository.RoomPictureRepository;
import com.sip.sipproject.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class RoomPictureService {
    @Autowired
    RoomPictureRepository roomPictureRepository;
    @Autowired
    RoomRepository roomRepository;

    public RoomPicture findById(Integer imgID) {
        Optional<RoomPicture> optional = roomPictureRepository.findById(imgID);
        if (optional.isEmpty()) {
            return null;
        }
        RoomPicture roomPicture = optional.get();
        return roomPicture;
    }

    public Boolean updateRoomPicture(List<MultipartFile> productImages, String productID) throws IOException {
        Optional<Room> optional = roomRepository.findById(productID);
        Room room = optional.get();
        if (optional.isEmpty()) {
            return false;
        }
//        List<RoomPicture> roomPictures = new ArrayList<>();
        for (MultipartFile productImage : productImages) {
            RoomPicture roomPicture = new RoomPicture();
            roomPicture.setImageData(productImage.getBytes());
            roomPicture.setRoom(room);// 建立多對一雙向關聯
            roomPictureRepository.save(roomPicture);
//            roomPictures.add(roomPicture);
        }
//        room.setRoomPictures(roomPictures);
//        roomPictureRepository.save(roomPicture);
        return true;
    }

    public List<RoomPicture> findPictureByRoom(Room  room) {
        return roomPictureRepository.findPictureByRoom(room);

    }

    public RoomPicture findPictureByID(Integer imgID) {
        Optional<RoomPicture> optional = roomPictureRepository.findById(imgID);
        if (optional.isPresent()) {
            RoomPicture roomPicture = optional.get();
            return roomPicture;
        } else {
            return null;
        }
    }

    public void deletePicture(Integer imgID) {
        roomPictureRepository.deleteById(imgID);
    }

    public Integer countByRoom(Room room) {
        return roomPictureRepository.countByProductID(room);
    }


}
