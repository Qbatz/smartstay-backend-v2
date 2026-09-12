package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.CustomerJobDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerJobDetailsRepository extends JpaRepository<CustomerJobDetails, Long> {

    @Query(value = """
            SELECT * FROM customer_job_details
            WHERE customer_id = :customerId AND hostel_id = :hostelId
              AND (is_deleted IS NULL OR is_deleted = false)
            ORDER BY job_id
            LIMIT 1
            """, nativeQuery = true)
    CustomerJobDetails findByCustomerIdAndHostelId(@Param("customerId") String customerId, @Param("hostelId") String hostelId);

    @Query("SELECT j FROM CustomerJobDetails j WHERE j.customerId = :customerId AND j.hostelId = :hostelId " +
           "AND (j.isDeleted IS NULL OR j.isDeleted = false) ORDER BY j.jobId")
    List<CustomerJobDetails> findActiveJobs(@Param("customerId") String customerId, @Param("hostelId") String hostelId);
}
