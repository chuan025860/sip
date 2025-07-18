package org.chyunn_web.service;

import org.chyunn_web.bean.Resource.ResourceBorrowRequest;
import org.chyunn_web.bean.User.User;
import org.chyunn_web.repository.ResourceBorrowRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ResourceBorrowRequestServie {
    @Autowired
    ResourceBorrowRequestRepository resourceBorrowRequestRepository;

    public void saveResourceBorrowr(ResourceBorrowRequest resourceBorrowRequest) {
        resourceBorrowRequestRepository.save(resourceBorrowRequest);
    }
    public List<ResourceBorrowRequest> findAllVisibleToUser(User creator ) {
       return resourceBorrowRequestRepository.findAllVisibleToUser(creator);
    }

    public Page<ResourceBorrowRequest> findByDateRange(
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    ) {
        return resourceBorrowRequestRepository.findByStartTimeBetween(start, end, pageable);
    }

    public ResourceBorrowRequest getResourceBorrowRequestById(Integer id) {
        Optional<ResourceBorrowRequest> optional = resourceBorrowRequestRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }else {
            return null;
        }
    }

    public Boolean deleteById(Integer id) {
        Optional<ResourceBorrowRequest> optional = resourceBorrowRequestRepository.findById(id);
        if (optional.isPresent()) {
            resourceBorrowRequestRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }



}
