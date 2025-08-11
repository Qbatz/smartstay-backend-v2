package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.responses.customer.CustomerData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomersRepository extends JpaRepository<Customers, String> {
    boolean existsByMobile(String mobileNo);

    @Query(value = """
    SELECT 
        first_name AS firstName,
        city,
        state,
        country,
        current_status AS currentStatus,
        customer_id as customerId,
        email_id AS emailId,
        profile_pic AS profilePic
    FROM customers 
    WHERE hostel_id = :hostelId
      AND (:name IS NULL OR LOWER(first_name) LIKE LOWER(CONCAT('%', :name, '%'))
                           OR LOWER(last_name) LIKE LOWER(CONCAT('%', :name, '%')))
      AND (:status IS NULL OR current_status = :status)
    """, nativeQuery = true)
    List<CustomerData> getCustomerData(
            @Param("hostelId") String hostelId,
            @Param("name") String name,
            @Param("status") String status
    );

}
