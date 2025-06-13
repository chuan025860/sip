package org.chyunn_web.service;

import org.chyunn_web.bean.Asset.Inventory_Equipment;
import org.chyunn_web.bean.Resource.BulletinPost;
import org.chyunn_web.dto.BulletinPostDto;
import org.chyunn_web.dto.Inventory_EquipmentDto;
import org.chyunn_web.repository.BulletinPostRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BulletinPostService {
    @Autowired
    private BulletinPostRepository bulletinPostRepository;
    public BulletinPost save(BulletinPost bulletinPost) {
        return bulletinPostRepository.save(bulletinPost);
    }
    public List<BulletinPost> findAll() {
        return bulletinPostRepository.findAll();
    }
    public BulletinPost findById(Integer id) {
        Optional<BulletinPost> bulletinPost = bulletinPostRepository.findById(id);
        if (bulletinPost.isPresent()) {
            return bulletinPost.get();
        }else {
            return  null;
        }
    }
    private ModelMapper modelMapper = new ModelMapper();

    public BulletinPostDto convertBulletinPostDto(BulletinPost bulletinPost) {
        // 自動映射
        return modelMapper.map(bulletinPost, BulletinPostDto.class);
    }
}
