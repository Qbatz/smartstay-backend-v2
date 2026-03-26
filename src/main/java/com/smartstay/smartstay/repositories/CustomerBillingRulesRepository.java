package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.CustomerBillingRules;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerBillingRulesRepository extends JpaRepository<CustomerBillingRules, String> {
}
