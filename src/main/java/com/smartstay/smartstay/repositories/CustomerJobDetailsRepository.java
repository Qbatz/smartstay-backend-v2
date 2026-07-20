package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.CustomerJobDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerJobDetailsRepository extends JpaRepository<CustomerJobDetails, Long> {
    CustomerJobDetails findByCustomerIdAndHostelId(String customerId, String hostelId);
}
