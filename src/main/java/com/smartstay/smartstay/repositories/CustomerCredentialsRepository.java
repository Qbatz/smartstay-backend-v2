package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.CustomerCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerCredentialsRepository extends JpaRepository<CustomerCredentials, String> {

    CustomerCredentials findByCustomerMobile(String mobile);
    CustomerCredentials findByXuid(String customerId);

    List<CustomerCredentials> findByCustomerMobileIn(List<String> mobiles);
}
