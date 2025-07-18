package org.chyunn_web.service;

import org.chyunn_web.bean.Resource.BulletinFile;
import org.chyunn_web.bean.incident.IncidentFile;
import org.chyunn_web.repository.BulletinFileRepostiory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BulletinFileService {
    @Autowired
    BulletinFileRepostiory bulletinFileRepostiory;
    public BulletinFile getBulletinFile(String imageId) {
        Optional<BulletinFile> optional = bulletinFileRepostiory.findById(imageId);
        if (optional.isPresent()) {
            BulletinFile bulletinFile = optional.get();
            return bulletinFile;
        } else {
            return null;
        }
    }

    public void deleteFile(BulletinFile bulletinFile) {
        try {
            bulletinFileRepostiory.delete(bulletinFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
