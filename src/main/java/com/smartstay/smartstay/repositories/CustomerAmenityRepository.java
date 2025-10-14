package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.CustomerAmenity;
import com.smartstay.smartstay.responses.amenitity.CustomerData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerAmenityRepository extends JpaRepository<CustomerAmenity, String> {

    boolean existsByAmenityIdAndCustomerId(String amenityId, String customerId);
    CustomerAmenity findTopByAmenityIdAndCustomerIdAndAssignedEndDateIsNullOrderByCreatedAtDesc(
            String amenityId,
            String customerId
    );


    CustomerAmenity findByAmenityIdAndCustomerId(String amenityId, String customerId);
    List<CustomerAmenity> findByAmenityId(String amenityId);

    @Query("""
                SELECT ca 
                FROM CustomerAmenity ca
                WHERE ca.amenityId = :amenityId
                  AND ca.createdAt = (
                      SELECT MAX(ca2.createdAt)
                      FROM CustomerAmenity ca2
                      WHERE ca2.amenityId = ca.amenityId
                        AND ca2.customerId = ca.customerId
                  )
            """)
    List<CustomerAmenity> findLatestByAmenityId(@Param("amenityId") String amenityId);


    @Query(value = """
                SELECT 
                    c.customer_id AS customerId,
                    CONCAT(c.first_name, ' ', c.last_name) AS customerName,
                    CASE 
                        WHEN ca.is_active = TRUE AND ca.assigned_end_date IS NULL
                        THEN 'ASSIGNED'
                        ELSE 'UNASSIGNED'
                    END AS status
                FROM customers c
                LEFT JOIN customer_amenity ca
                    ON ca.customer_id = c.customer_id
                   AND ca.amenity_id = :amenityId
                   AND ca.created_at = (
                        SELECT MAX(ca2.created_at)
                        FROM customer_amenity ca2
                        WHERE ca2.customer_id = c.customer_id
                          AND ca2.amenity_id = :amenityId
                    )
                WHERE c.hostel_id = :hostelId
            """, nativeQuery = true)
    List<CustomerData> findCustomersWithAmenityStatus(@Param("amenityId") String amenityId, @Param("hostelId") String hostelId);









}
