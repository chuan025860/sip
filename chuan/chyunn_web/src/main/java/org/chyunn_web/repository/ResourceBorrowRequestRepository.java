package org.chyunn_web.repository;

import org.chyunn_web.bean.Resource.ResourceBorrowRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface ResourceBorrowRequestRepository extends JpaRepository<ResourceBorrowRequest, Integer> {
    @Query("SELECT r FROM ResourceBorrowRequest r WHERE r.startTime >= :start AND r.startTime <= :end ORDER BY r.startTime ASC ")
    Page<ResourceBorrowRequest> findByStartTimeBetween(
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );
}
