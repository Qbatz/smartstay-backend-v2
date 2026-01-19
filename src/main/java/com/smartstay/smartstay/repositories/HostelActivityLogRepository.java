package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.HostelActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HostelActivityLogRepository extends JpaRepository<HostelActivityLog, Long> {

//    @Query("SELECT h FROM HostelActivityLog h WHERE h.hostelId = :hostelId AND (:search IS NULL OR :search = '' OR LOWER(h.description) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(h.eventType) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(h.source) LIKE LOWER(CONCAT('%', :search, '%')))")
//    Page<HostelActivityLog> searchByHostelId(@Param("hostelId") String hostelId, @Param("search") String search,
//            Pageable pageable);

    @Query("SELECT h FROM HostelActivityLog h WHERE h.hostelId = :hostelId")
    Page<HostelActivityLog> searchByHostelId(@Param("hostelId") String hostelId,
                                             Pageable pageable);
}
