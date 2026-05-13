package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.RentHistory;
import com.smartstay.smartstay.dto.rentHistory.UpcomingRents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface RentHistoryRepository extends JpaRepository<RentHistory, Long> {
    @Query(value = """
            SELECT * FROM rent_history WHERE customer_id=:customerId limit 1
            """, nativeQuery = true)
    RentHistory findByCustomerId(@Param("customerId") String customerId);

    @Query(value = """
            SELECT * FROM rent_history WHERE customer_id=:customerId and starts_from <= DATE(:date) ORDER by starts_from DESC LIMIT 1
            """, nativeQuery = true)
    RentHistory findRentByCustomerIdAndDate(@Param("customerId") String customerId, @Param("date") Date date);

    @Query(value = """
            SELECT * FROM rent_history WHERE customer_id=:customerId AND DATE(starts_from) <= DATE(:endDate) AND 
            (ending_at IS NULL OR DATE(ending_at) >= :startDate)
            ORDER by starts_from DESC LIMIT 1
            """, nativeQuery = true)
    RentHistory findCurrentMonthRent(@Param("customerId") String customerId, @Param("startDate") Date date, @Param("endDate") Date endDate);

    @Query(value = """
            SELECT * FROM `rent_history` WHERE DATE(starts_from) = DATE(:startsFrom)
            """, nativeQuery = true)
    List<RentHistory> findRentApplyFromDate(@Param("startsFrom") Date startsFrom);

    @Query(value = """
            SELECT * FROM rent_history WHERE customer_id = :customerId AND DATE(starts_from) >= DATE(:startsFrom) limit 1
            """, nativeQuery = true)
    RentHistory findByCustomerIdAndStartsFrom(@Param("customerId") String customerId, @Param("startsFrom") Date startsFrom);

}
