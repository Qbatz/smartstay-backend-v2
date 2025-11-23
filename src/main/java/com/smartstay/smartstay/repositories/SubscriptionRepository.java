package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Subscription findByHostelId(String hostelId);

    @Query(value = """
            SELECT * FROM subscription WHERE hostel_id=:hostelId and DATE(plan_starts_at) <= DATE(:todaysDate) 
            and DATE(plan_ends_at) >=DATE(:todaysDate) LIMIT 1
            """, nativeQuery = true)
    Subscription checkSubscriptionForToday(@Param("hostelId") String hostelId, @Param("todaysDate") Date todaysDate);
}
