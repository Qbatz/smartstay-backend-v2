package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.CustomerCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerCredentialsRepository extends JpaRepository<CustomerCredentials, String> {

    CustomerCredentials findByCustomerMobile(String mobile);
}
