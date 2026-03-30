package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.BillingRules;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface BillingRuleRepository extends JpaRepository<BillingRules, Integer> {


    @Query("SELECT b FROM BillingRules b WHERE b.id = :billingRuleId AND b.hostel.id = :hostelId")
    Optional<BillingRules> findBillingRuleByIdAndHostelId(@Param("billingRuleId") Integer billingRuleId,
                                                          @Param("hostelId") String hostelId);
    Optional<BillingRules> findByHostel_hostelId(String hostelId);

    @Query(value = """
            SELECT * FROM billing_rules WHERE (start_from IS NULL OR DATE(start_from) <=DATE(:startDate)) 
            AND hostel_id=:hostelId
            ORDER BY billing_start_date DESC LIMIT 1
            """, nativeQuery = true)
    BillingRules findByHostelIdAndStartDate(@Param("hostelId") String hostelId, @Param("startDate") Date startDate);

    @Query(value = """
            SELECT * FROM billing_rules WHERE DATE(start_from) >=DATE(:startDate) AND hostel_id=:hostelId ORDER BY start_from DESC LIMIT 1
            """, nativeQuery = true)
    BillingRules findNewRuleByHostelIdAndDate(@Param("hostelId") String hostelId, @Param("startDate") Date startDate);

    @Query(value = """
           SELECT * FROM billing_rules WHERE hostel_id=:hostelId ORDER BY created_at DESC LIMIT 1
            """, nativeQuery = true)
    BillingRules findCurrentBillingRules(@Param("hostelId") String hostelId);

    @Query(value = """
            SELECT * FROM billing_rules b WHERE b.billing_start_date =:day 
                AND b.billing_model='PREPAID' AND b.type_of_billing='FIXED_DATE' 
                      AND b.created_at = (
                          SELECT MAX(b2.created_at)
                          FROM billing_rules b2
                          WHERE b2.hostel_id = b.hostel_id
                            group by b2.hostel_id
                      )
            """, nativeQuery = true)
    List<BillingRules> findAllHostelsHavingTodaysRecurring(@Param("day") String day);

    @Query(value = """
            SELECT * FROM billing_rules b WHERE b.billing_start_date =:day 
                AND b.billing_model='PREPAID' AND b.type_of_billing='FIXED_DATE' 
                      AND b.created_at = (
                          SELECT MAX(b2.created_at)
                          FROM billing_rules b2
                          WHERE b2.hostel_id = b.hostel_id
                            group by b2.hostel_id
                      )
            """, nativeQuery = true)
    List<BillingRules> findAllHostelsHavingTodaysRecurring(@Param("day") Integer day);

    @Query(value = """
            SELECT * FROM billing_rules br WHERE br.created_at=(SELECT MAX(br2.created_at) from billing_rules br2 WHERE br2.hostel_id=br.hostel_id)  
            AND br.should_notify=true
            """, nativeQuery = true)
    List<BillingRules> findAllHostelsHavingReminders();

    @Query(value = """
             SELECT * FROM billing_rules b WHERE b.billing_start_date =:day 
                AND b.billing_model='POSTPAID' AND b.type_of_billing='FIXED_DATE' 
                 AND b.created_at = (
                          SELECT MAX(b2.created_at)
                          FROM billing_rules b2
                          WHERE b2.hostel_id = b.hostel_id
                            group by b2.hostel_id
                      )
            """, nativeQuery = true)
    List<BillingRules> findPostpaidHostelsHavingBillingRuleTomorrow(@Param("day") Integer day);

    @Query(value = """
             SELECT * FROM billing_rules b WHERE b.billing_model='PREPAID' AND b.type_of_billing='JOINING_DATE_BASED' 
                 AND b.created_at = (
                          SELECT MAX(b2.created_at)
                          FROM billing_rules b2
                          WHERE b2.hostel_id = b.hostel_id
                            group by b2.hostel_id
                      )
            """, nativeQuery = true)
    List<BillingRules> findHostelsHavingJoiningDateBasedBillings();
}
