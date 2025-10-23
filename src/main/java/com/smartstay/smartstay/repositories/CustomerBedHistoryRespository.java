package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.CustomersBedHistory;
import com.smartstay.smartstay.dto.customer.BedHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerBedHistoryRespository extends JpaRepository<CustomersBedHistory, Long> {
    @Query(value = """
            SELECT * FROM customers_bed_history WHERE customer_id=:customerId and start_date <= DATE(:startDate) and (end_date IS NULL OR end_date <= (:endDate))
            ORDER BY start_date DESC LIMIT 1
            """, nativeQuery = true)
    CustomersBedHistory findByCustomerIdAndDate(@Param("customerId") String customerId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query(value = """
            SELECT * FROM customers_bed_history cbh WHERE cbh.hostel_id=:hostelId AND cbh.start_date<=DATE(:endDate) AND (cbh.end_date IS NULL OR end_date >=DATE(:startDate))
            """, nativeQuery = true)
    List<CustomersBedHistory> findByHostelIdAndStartAndEndDate(@Param("hostelId") String hostelId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

    Optional<CustomersBedHistory> findTopByCustomerIdOrderByCreatedAtDesc(String customerId);

    @Query(value = """
            SELECT cbh.id as historyId, cbh.bed_id as bedId, bed.bed_name as bedName, rms.room_name as roomName, 
            rms.room_id as roomId, cbh.start_date as startDate, cbh.end_date as endDate, cbh.reason, 
            cbh.rent_amount as rent, cbh.type as type FROM customers_bed_history cbh LEFT OUTER JOIN beds bed on bed.bed_id=cbh.bed_id 
            LEFT OUTER JOIN rooms rms on rms.room_id=bed.room_id where cbh.customer_id=:customerId
            """, nativeQuery = true)
    List<BedHistory> findByCustomerId(@Param("customerId") String customerId);
}
