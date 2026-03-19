package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.CustomerAdditionalContacts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerAdditionalContactsRepositories extends JpaRepository<CustomerAdditionalContacts, Long> {
    @Query(value = """
            SELECT * FROM customer_additional_contacts cac WHERE cac.hostel_id=:hostelId AND cac.customer_id=:customerId 
            AND cac.is_deleted=false
            """, nativeQuery = true)
    List<CustomerAdditionalContacts> findByCustomerIdAndHostelId(@Param("hostelId") String hostelId, @Param("customerId") String customerId);
}
