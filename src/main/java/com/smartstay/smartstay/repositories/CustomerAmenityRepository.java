package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.CustomersAmenity;
import com.smartstay.smartstay.responses.amenitity.CustomerData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerAmenityRepository extends JpaRepository<CustomersAmenity, String> {

    CustomersAmenity findTopByAmenityIdAndCustomerIdAndEndDateIsNullOrderByCreatedAtDesc(
            String amenityId,
            String customerId
    );

    @Query("""
                SELECT ca 
                FROM CustomersAmenity ca
                WHERE ca.amenityId = :amenityId
                  AND ca.createdAt = (
                      SELECT MAX(ca2.createdAt)
                      FROM CustomersAmenity ca2
                      WHERE ca2.amenityId = ca.amenityId
                        AND ca2.customerId = ca.customerId
                  )
            """)
    List<CustomersAmenity> findLatestByAmenityId(@Param("amenityId") String amenityId);


    @Query(value = """
                SELECT 
                    c.customer_id AS customerId,
                    CONCAT(c.first_name, ' ', c.last_name) AS customerName,
                    CASE 
                        WHEN ca.end_date IS NULL and ca.start_date is not null
                        THEN 'ASSIGNED'
                        ELSE 'UNASSIGNED'
                    END AS status
                FROM customers c
                LEFT JOIN customers_amenity ca
                    ON ca.customer_id = c.customer_id
                   AND ca.amenity_id = :amenityId
                   AND ca.created_at = (
                        SELECT MAX(ca2.created_at)
                        FROM customers_amenity ca2
                        WHERE ca2.customer_id = c.customer_id
                          AND ca2.amenity_id = :amenityId
                    )
                WHERE c.hostel_id = :hostelId
            """, nativeQuery = true)
    List<CustomerData> findCustomersWithAmenityStatus(@Param("amenityId") String amenityId, @Param("hostelId") String hostelId);

    List<CustomersAmenity> findByCustomerId(String customerId);

}
