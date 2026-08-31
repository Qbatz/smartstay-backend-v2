package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.KycDetails;
import com.smartstay.smartstay.dto.kyc.KycUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KycRepository extends JpaRepository<KycDetails, Long> {
    @Query("""
            SELECT kd FROM KycDetails kd WHERE kd.currentStatus = 'REQUESTED'
            """)
    List<KycDetails> findAllRequested();

    @Query(value = """
            SELECT hostel.hostel_id, hostel_name, count(kyc.id) as count,
            max(kyc.created_at) as latest_request, max(kyc.updated_at) as latest_verified,
            count(kyc.current_status) as request_count
            FROM kyc_details kyc inner join customers cus on cus.customer_id=kyc.customer_id 
            inner join hostelv1 hostel on hostel.hostel_id=cus.hostel_id group by hostel.hostel_id;
            """, nativeQuery = true)
    List<KycUsage> findAllRequestedHostels();

    @Query(value = """
            SELECT * FROM kyc_details kd WHERE kd.customer_id IN (:customerIds) ORDER BY kd.created_at DESC LIMIT 1
            """, nativeQuery = true)
    KycDetails findLatestRequest(@Param("customerIds") List<String> customerIds);

    @Query(value = """
            SELECT * FROM kyc_details kd WHERE kd.customer_id IN (:customerIds) 
            AND kd.current_status='VERIFIED' ORDER BY kd.completed_at DESC LIMIT 1
            """, nativeQuery = true)
    KycDetails findLatestCompletion(@Param("customerIds") List<String> customerIds);

    @Query(value = """
            SELECT * FROM kyc_details kd WHERE kd.customer_id IN (:customerIds)
            """, nativeQuery = true)
    List<KycDetails> findByCustomerIds(@Param("customerIds") List<String> customerIds);
}
