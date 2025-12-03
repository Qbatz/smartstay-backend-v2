package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.CustomersConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomersConfigRepository extends JpaRepository<CustomersConfig, Long> {
    List<CustomersConfig> findByHostelIdAndIsActiveTrue(String customerId);
    CustomersConfig findByCustomerId(String customerId);

    @Query(value = """
            SELECT * FROM customers_config WHERE hostel_id=:hostelId AND is_active=true AND enabled=true
            """, nativeQuery = true)
    List<CustomersConfig> findActiveAndRecurringEnabledCustomersByHostelId(@Param("hostelId") String hostelId);
}
