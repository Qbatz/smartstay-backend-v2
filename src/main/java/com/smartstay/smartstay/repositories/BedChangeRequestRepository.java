package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.BedChangeRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BedChangeRequestRepository extends JpaRepository<BedChangeRequest, Long> {

    @Query("SELECT bcr FROM BedChangeRequest bcr WHERE bcr.hostelId = :hostelId AND bcr.isActive = true AND bcr.isDeleted = false ORDER BY bcr.createdAt DESC")
    List<BedChangeRequest> findTopRequests(@Param("hostelId") String hostelId, Pageable pageable);
}
