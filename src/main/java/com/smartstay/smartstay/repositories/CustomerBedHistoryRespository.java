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
            SELECT * FROM customers_bed_history WHERE customer_id=:customerId and DATE(start_date) <= DATE(:endDate) and (end_date IS NULL OR DATE(end_date) >= DATE(:startDate))
            ORDER BY start_date DESC LIMIT 1
            """, nativeQuery = true)
    CustomersBedHistory findByCustomerIdAndDate(@Param("customerId") String customerId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query(value = """
            SELECT * FROM customers_bed_history WHERE customer_id=:customerId
            """, nativeQuery = true)
    List<CustomersBedHistory> listBedsByCustomerIdAndDate(@Param("customerId") String customerId);

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

    @Query(value = """
            SELECT * FROM customers_bed_history WHERE customer_id=:customerId AND type='BOOKED' LIMIT 1
            """, nativeQuery = true)
    CustomersBedHistory findByCustomerIdAndTypeBooking(@Param("customerId") String customerId);

    @Query("""
            SELECT cbh FROM CustomersBedHistory cbh where cbh.type='BOOKED'
            """)
    List<CustomersBedHistory> findByAllBookings();

    @Query(value = """
            SELECT * FROM customers_bed_history WHERE customer_id=:customerId and type='CHECK_IN' LIMIT 1
            """, nativeQuery = true)
    CustomersBedHistory findByCustomerIdAndTypeRent(@Param("customerId") String customerId);

    @Query(value = """
            SELECT * FROM customers_bed_history WHERE customer_id=:customerId and type in ('CHECK_IN', 'REASSIGNED')
            """, nativeQuery = true)
    List<CustomersBedHistory> findAllBedsAfterJoining(@Param("customerId") String customerId);

    @Query(value = """
            SELECT * FROM customers_bed_history cbh WHERE cbh.customer_id=:customerId AND 
            DATE(cbh.start_date) <=DATE(:endDate) AND (cbh.end_date IS NULL OR DATE(cbh.end_date) >= DATE(:startDate))
            """, nativeQuery = true)
    List<CustomersBedHistory> findByCustomerIdAndStartAndEndDate(@Param("customerId") String customerId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query(value = """
            SELECT * FROM customers_bed_history WHERE end_date IS NULL AND type in ('CHECK_IN', 'REASSIGNED') AND customer_id in (:customerId)
            """, nativeQuery = true)
    List<CustomersBedHistory> findCurrentBed(@Param("customerId") List<String> customerId);

    @Query(value = """
    SELECT cbh.*  FROM customers_bed_history cbh INNER JOIN (
        SELECT customer_id, MAX(id) AS latest_id
        FROM customers_bed_history
        WHERE customer_id IN (:customerIds)
        GROUP BY customer_id
    ) t ON cbh.id = t.latest_id
    """, nativeQuery = true)
    List<CustomersBedHistory> findLatestBedHistoryForCustomers(@Param("customerIds") List<String> customerIds);

    @Query(value = """
            SELECT * FROM customers_bed_history cbh where cbh.room_id=:roomId AND DATE(cbh.start_date) <= DATE(:endDate) AND (cbh.end_date IS NULL OR DATE(cbh.end_date) >= DATE(:startDate))
            """, nativeQuery = true)
    List<CustomersBedHistory> findByRoomIdStartAndEndDate(@Param("roomId") Integer roomId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
}

