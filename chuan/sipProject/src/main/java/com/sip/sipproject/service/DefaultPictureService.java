package com.sip.sipproject.service;

import com.sip.sipproject.bean.DefaultPicture;
import com.sip.sipproject.repository.DefaultPictureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DefaultPictureService {
    @Autowired
    DefaultPictureRepository defaultPictureRepository;
    public DefaultPicture findPictureById(Integer DefaultPictureID){
      Optional<DefaultPicture> optional=defaultPictureRepository.findById(DefaultPictureID);
        if (optional.isPresent()) {
            DefaultPicture defaultPicture = optional.get();
            return defaultPicture;
        } else {
            return null;
        }
    }
}
