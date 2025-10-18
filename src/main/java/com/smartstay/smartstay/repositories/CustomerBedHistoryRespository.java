package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.CustomersBedHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface CustomerBedHistoryRespository extends JpaRepository<CustomersBedHistory, Long> {
    @Query(value = """
            SELECT * FROM customers_bed_history WHERE customer_id=:customerId and start_date <= DATE(:startDate) and (end_date IS NULL OR end_date <= (:endDate))
            """, nativeQuery = true)
    CustomersBedHistory findByCustomerIdAndDate(@Param("customerId") String customerId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
