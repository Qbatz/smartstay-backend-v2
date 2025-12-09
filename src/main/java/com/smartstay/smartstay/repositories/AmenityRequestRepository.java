package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.AmenityRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AmenityRequestRepository extends JpaRepository<AmenityRequest, Long> {

    @Query("SELECT ar FROM AmenityRequest ar WHERE ar.customerId=:customerId AND ar.hostelId=:hostelId AND currentStatus IN('PENDING', 'OPEN')")
    List<AmenityRequest> findByCustomerIdAndHostelId(String customerId, String hostelId);
}
