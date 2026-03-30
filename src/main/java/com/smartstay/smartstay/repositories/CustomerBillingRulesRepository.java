package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.CustomerBillingRules;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerBillingRulesRepository extends JpaRepository<CustomerBillingRules, String> {

    @Query("""
            SELECT cbr FROM CustomerBillingRules cbr WHERE cbr.billingDay=:billingDay AND cbr.hostelId in (:hostelIds)
            """)
    List<CustomerBillingRules> findCustomersHavingBillingToday(List<String> hostelIds, int billingDay);
}
