package org.chyunn_web.repository;

import org.chyunn_web.bean.incident.Incident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface IncidentRepository extends JpaRepository<Incident, Integer> {
    @Query("SELECT i FROM Incident i WHERE i.invalid = false " +
            "ORDER BY CASE i.status " +
            "WHEN '未開始' THEN 1 " +
            "WHEN '待處理' THEN 2 " +
            "WHEN '處理中' THEN 3 " +
            "WHEN '已轉交他人' THEN 4 " +
            "WHEN '延期' THEN 5 " +
            "WHEN '待結案(待收款)' THEN 6 " +
            "WHEN '退件' THEN 7 " +
            "WHEN '已結案' THEN 8 " +
            "WHEN '已作廢' THEN 9 " +
            "ELSE 10 END, i.lastReportTime DESC")
    Page<Incident> findAllByReportDateDescAndInvalidFalse(Pageable pageable);

    //有需要再加上 i.invalid = false
    @Query("SELECT i FROM Incident i WHERE  " +
            "(:status IS NULL OR i.status = :status) " +
            "AND (:importance IS NULL OR i.importance = :importance) " +
            "AND (:searchInput IS NULL OR i.eventContent LIKE %:searchInput% OR i.eventPurpose LIKE %:searchInput%) " +
            "AND (:startDate IS NULL OR i.reportDate >= :startDate) " +
            "AND (:endDate IS NULL OR i.reportDate <= :endDate) " +
            "ORDER BY CASE i.status " +
            "WHEN '未開始' THEN 1 " +
            "WHEN '待處理' THEN 2 " +
            "WHEN '處理中' THEN 3 " +
            "WHEN '已轉交他人' THEN 4 " +
            "WHEN '延期' THEN 5 " +
            "WHEN '待結案(待收款)' THEN 6 " +
            "WHEN '退件' THEN 7 " +
            "WHEN '已結案' THEN 8 " +
            "WHEN '已作廢' THEN 9 " +
            "ELSE 10 END, i.lastReportTime DESC")
    Page<Incident> findAllByStatusAndSearchInputAndDateRange(Pageable pageable,
                                                             @Param("status") String status,
                                                             @Param("importance") String importance,
                                                             @Param("searchInput") String searchInput,
                                                             @Param("startDate") Date startDate,
                                                             @Param("endDate") Date endDate);

    @Query("SELECT MAX(i.incidentId) FROM Incident i")
    Integer findMaxIncidentId();

    @Query("SELECT COUNT(i) FROM Incident i WHERE i.status = :status")
    Integer getStatusCounts(@Param("status") String status);

    @Query("SELECT COUNT(i) FROM Incident i WHERE i.lastReportTime >= :tenDaysAgo AND i.reportCount != 0")
    Integer getRecentReportsCounts(@Param("tenDaysAgo") LocalDateTime tenDaysAgo);

    @Query("SELECT i FROM Incident i WHERE i.status != '已作廢' AND i.visibility = true ORDER BY i.reportDate DESC")
    List<Incident> findTop5VisibleNotDiscardedOrderByLastReportTimeDesc(Pageable pageable);

    @Query("SELECT i FROM Incident i WHERE i.lastReportTime >= :tenDaysAgo AND i.reportCount != 0" +
            "ORDER BY CASE i.status " +
            "WHEN '未開始' THEN 1 " +
            "WHEN '待處理' THEN 2 " +
            "WHEN '處理中' THEN 3 " +
            "WHEN '已轉交他人' THEN 4 " +
            "WHEN '延期' THEN 5 " +
            "WHEN '待結案(待收款)' THEN 6 " +
            "WHEN '退件' THEN 7 " +
            "WHEN '已結案' THEN 8 " +
            "WHEN '已作廢' THEN 9 " +
            "ELSE 10 END, i.lastReportTime DESC")
    Page<Incident> getRecentReports(@Param("tenDaysAgo") LocalDateTime tenDaysAgo, Pageable pageable);

    @Query("SELECT i FROM Incident i WHERE i.incidentId < :currentId ORDER BY i.incidentId DESC")
    List<Incident> findPreviousCandidates(@Param("currentId") Integer currentId);

    @Query("SELECT i FROM Incident i WHERE i.incidentId > :currentId ORDER BY i.incidentId ASC")
    List<Incident> findNextCandidates(@Param("currentId") Integer currentId);

}
