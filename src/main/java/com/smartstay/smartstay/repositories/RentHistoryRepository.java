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
            SELECT * FROM rent_history WHERE customer_id=:customerId AND (is_active = true OR is_active IS NULL) limit 1
            """, nativeQuery = true)
    RentHistory findByCustomerId(@Param("customerId") String customerId);

    @Query(value = """
            SELECT * FROM rent_history WHERE customer_id=:customerId AND DATE(starts_from) <= DATE(:date) AND (is_active = true OR is_active IS NULL) ORDER by starts_from DESC LIMIT 1
            """, nativeQuery = true)
    RentHistory findRentByCustomerIdAndDate(@Param("customerId") String customerId, @Param("date") Date date);

    @Query(value = """
            SELECT * FROM rent_history WHERE customer_id=:customerId AND DATE(starts_from) <= DATE(:endDate) AND 
            (ending_at IS NULL OR DATE(ending_at) >= :startDate) AND (is_active = true OR is_active IS NULL)
            ORDER by starts_from DESC LIMIT 1
            """, nativeQuery = true)
    RentHistory findCurrentMonthRent(@Param("customerId") String customerId, @Param("startDate") Date date, @Param("endDate") Date endDate);

    @Query(value = """
            SELECT * FROM `rent_history` WHERE DATE(starts_from) = DATE(:startsFrom) AND (is_active = true OR is_active IS NULL)
            """, nativeQuery = true)
    List<RentHistory> findRentApplyFromDate(@Param("startsFrom") Date startsFrom);

    @Query(value = """
            SELECT * FROM rent_history WHERE customer_id = :customerId AND DATE(starts_from) >= DATE(:startsFrom) AND (is_active = true OR is_active IS NULL) limit 1
            """, nativeQuery = true)
    RentHistory findByCustomerIdAndStartsFrom(@Param("customerId") String customerId, @Param("startsFrom") Date startsFrom);

    @Query(value = """
            SELECT * FROM rent_history WHERE customer_id = :customerId AND DATE(starts_from) > DATE(:date) AND (is_active = true OR is_active IS NULL) ORDER BY starts_from desc LIMIT 1
            """, nativeQuery = true)
    RentHistory findNextUpcomingRentHistoryByCustomerIdAndAfterDate(@Param("customerId") String customerId, @Param("date") Date date);

}
