package org.chyunn_web.service;

import org.chyunn_web.bean.ResourceBorrowRequest;
import org.chyunn_web.bean.User;
import org.chyunn_web.repository.ResourceBorrowRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResourceBorrowRequestServie {
    @Autowired
    ResourceBorrowRequestRepository resourceBorrowRequestRepository;

    public void saveResourceBorrowr(ResourceBorrowRequest resourceBorrowRequest) {
        resourceBorrowRequestRepository.save(resourceBorrowRequest);
    }
    public List<ResourceBorrowRequest> findAll( ) {
       return resourceBorrowRequestRepository.findAll();
    }

}
