package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.RentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.util.Date;

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
}
