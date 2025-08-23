package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.dto.customer.CustomerData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomersRepository extends JpaRepository<Customers, String> {
    boolean existsByMobile(String mobileNo);

    boolean existsByEmailId(String emailId);

    @Query(value = """
    SELECT cus.first_name AS firstName, cus.city, cus.mobile, cus.state, cus.joining_date, 
    cus.created_at, cus.country, cus.current_status AS currentStatus, 
    cus.customer_id as customerId, cus.email_id AS emailId, 
    cus.profile_pic AS profilePic, cus.joining_date as actualJoiningDate, cus.exp_joining_date as joiningDate, 
    cus.created_at as createdAt FROM customers cus
    WHERE cus.hostel_id = :hostelId
    AND (:name IS NULL OR LOWER(cus.first_name) LIKE LOWER(CONCAT('%', :name, '%'))
                           OR LOWER(cus.last_name) LIKE LOWER(CONCAT('%', :name, '%')))
      AND (:status IS NULL OR cus.current_status = :status)
    """, nativeQuery = true)
    List<CustomerData> getCustomerData(
            @Param("hostelId") String hostelId,
            @Param("name") String name,
            @Param("status") String status
    );

}
