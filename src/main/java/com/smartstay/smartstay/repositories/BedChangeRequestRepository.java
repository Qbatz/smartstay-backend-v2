package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.BedChangeRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface BedChangeRequestRepository extends JpaRepository<BedChangeRequest, Long> {

    @Query("SELECT bcr FROM BedChangeRequest bcr WHERE bcr.hostelId = :hostelId AND bcr.isActive = true AND bcr.isDeleted = false ORDER BY bcr.createdAt DESC")
    List<BedChangeRequest> findTopRequests(@Param("hostelId") String hostelId, Pageable pageable);

    @Query("""
            SELECT COUNT(bcr) as total,
            SUM(CASE WHEN bcr.currentStatus IN ('PENDING', 'OPEN') THEN 1 ELSE 0 END) as pending,
             SUM(CASE WHEN bcr.currentStatus = 'IN_PROGRESS' THEN 1 ELSE 0 END) as inProgress,
            SUM(CASE WHEN bcr.currentStatus = 'RESOLVED' THEN 1 ELSE 0 END) as resolved
            FROM BedChangeRequest bcr
            WHERE bcr.hostelId = :hostelId
            AND (:startDate IS NULL OR DATE(bcr.createdAt) >= DATE(:startDate))
            AND (:endDate IS NULL OR DATE(bcr.createdAt) <= DATE(:endDate))
            """)
    Map<String, Object> getRequestStatusSummary(@Param("hostelId") String hostelId, @Param("startDate") java.util.Date startDate, @Param("endDate") java.util.Date endDate);
}
