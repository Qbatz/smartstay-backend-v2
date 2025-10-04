package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.CustomerAmenity;
import com.smartstay.smartstay.responses.amenitity.CustomerData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerAmenityRepository extends JpaRepository<CustomerAmenity, String> {

    boolean existsByAmenityIdAndCustomerId(String amenityId, String customerId);

    CustomerAmenity findByAmenityIdAndCustomerId(String amenityId, String customerId);

    @Query(value = """
    SELECT c.customer_id as customerId,
           CONCAT(c.first_name, ' ', c.last_name) as customerName,
           CASE WHEN ca.amenity_id IS NOT NULL 
                THEN 'ASSIGNED'
                ELSE 'UNASSIGNED' END as status
    FROM customers c
    LEFT JOIN customer_amenity ca
           ON ca.customer_id = c.customer_id
          AND ca.amenity_id = :amenityId
    WHERE c.hostel_id = :hostelId
""", nativeQuery = true)
    List<CustomerData> findCustomersWithAmenityStatus(
            @Param("amenityId") String amenityId,
            @Param("hostelId") String hostelId
    );





}
