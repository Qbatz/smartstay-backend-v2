package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.BillingRules;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BillingRuleRepository extends JpaRepository<BillingRules, Integer> {


    @Query("SELECT b FROM BillingRules b WHERE b.id = :billingRuleId AND b.hostel.id = :hostelId")
    Optional<BillingRules> findBillingRuleByIdAndHostelId(@Param("billingRuleId") Integer billingRuleId,
                                                          @Param("hostelId") String hostelId);
    Optional<BillingRules> findByHostel_hostelId(String hostelId);
}
