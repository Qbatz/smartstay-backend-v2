package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.AmenityRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface AmenityRequestRepository extends JpaRepository<AmenityRequest, Long> {

        @Query("SELECT ar FROM AmenityRequest ar WHERE ar.customerId=:customerId AND ar.hostelId=:hostelId AND currentStatus IN('PENDING', 'OPEN')")
        List<AmenityRequest> findByCustomerIdAndHostelId(String customerId, String hostelId);

        @Query("SELECT COUNT(ar) FROM AmenityRequest ar WHERE ar.hostelId = :hostelId AND DATE(ar.createdAt) >= DATE(:startDate) AND DATE(ar.createdAt) <= DATE(:endDate)")
        int countByHostelIdAndDateRange(@Param("hostelId") String hostelId, @Param("startDate") Date startDate,
                        @Param("endDate") Date endDate);

        @Query("SELECT COUNT(ar) FROM AmenityRequest ar WHERE ar.hostelId = :hostelId AND ar.currentStatus IN :statuses AND DATE(ar.createdAt) >= DATE(:startDate) AND DATE(ar.createdAt) <= DATE(:endDate)")
        int countActiveByHostelIdAndDateRange(@Param("hostelId") String hostelId,
                        @Param("statuses") List<String> statuses,
                        @Param("startDate") java.util.Date startDate, @Param("endDate") java.util.Date endDate);

        @Query("""
                        SELECT COUNT(ar) as total,
                        SUM(CASE WHEN ar.currentStatus IN ('PENDING', 'OPEN', 'IN_PROGRESS') THEN 1 ELSE 0 END) as pending,
                        SUM(CASE WHEN ar.currentStatus = 'RESOLVED' THEN 1 ELSE 0 END) as resolved
                        FROM AmenityRequest ar
                        WHERE ar.hostelId = :hostelId
                        AND (:startDate IS NULL OR DATE(ar.createdAt) >= DATE(:startDate))
                        AND (:endDate IS NULL OR DATE(ar.createdAt) <= DATE(:endDate))
                        """)
        java.util.Map<String, Object> getRequestStatusSummary(@Param("hostelId") String hostelId,
                        @Param("startDate") java.util.Date startDate, @Param("endDate") java.util.Date endDate);

        @Query("SELECT ar FROM AmenityRequest ar WHERE ar.hostelId = :hostelId ORDER BY ar.createdAt DESC")
        List<AmenityRequest> findTopRequests(@Param("hostelId") String hostelId, org.springframework.data.domain.Pageable pageable);
}
