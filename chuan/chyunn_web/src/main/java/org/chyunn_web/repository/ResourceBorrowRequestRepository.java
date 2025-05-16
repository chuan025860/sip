package org.chyunn_web.repository;

import org.chyunn_web.bean.ResourceBorrowRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceBorrowRequestRepository extends JpaRepository<ResourceBorrowRequest, Integer> {
}
