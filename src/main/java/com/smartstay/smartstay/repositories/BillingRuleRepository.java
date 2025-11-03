package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.BillingRules;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.Optional;

public interface BillingRuleRepository extends JpaRepository<BillingRules, Integer> {


    @Query("SELECT b FROM BillingRules b WHERE b.id = :billingRuleId AND b.hostel.id = :hostelId")
    Optional<BillingRules> findBillingRuleByIdAndHostelId(@Param("billingRuleId") Integer billingRuleId,
                                                          @Param("hostelId") String hostelId);
    Optional<BillingRules> findByHostel_hostelId(String hostelId);

    @Query(value = """
            SELECT * FROM billing_rules WHERE start_from IS NULL OR start_from <=DATE(:startDate) 
            AND hostel_id=:hostelId
            ORDER BY billing_start_date DESC LIMIT 1
            """, nativeQuery = true)
    BillingRules findByHostelIdAndStartDate(@Param("hostelId") String hostelId, @Param("startDate") Date startDate);

    @Query(value = """
            SELECT * FROM billing_rules WHERE start_from >=DATE(:startDate) AND hostel_id=:hostelId ORDER BY start_from DESC LIMIT 1
            """, nativeQuery = true)
    BillingRules findNewRuleByHostelIdAndDate(@Param("hostelId") String hostelId, @Param("startDate") Date startDate);

    @Query(value = """
            SELECT * FROM billing_rules WHERE hostel_id=:hostelId AND start_from IS NULL OR start_from <= DATE(:startDate) ORDER BY start_from DESC LIMIT 1
            """, nativeQuery = true)
    BillingRules findLatestBillingRule(@Param("hostelId") String hostelId, @Param("startDate") Date startDate);

    @Query(value = """
            SELECT * FROM billing_rules WHERE hostel_id=:hostelId AND start_from IS NULL OR start_from <= DATE(:startDate) ORDER BY start_from DESC LIMIT 1
            """, nativeQuery = true)
    BillingRules findBillingRulesOnDateAndHostelId(@Param("hostelId") String hostel, @Param("startDate") Date date);
}
