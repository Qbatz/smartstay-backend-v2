package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.RecurringTracker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecurringTrackerRepository extends JpaRepository<RecurringTracker, Long> {
    @Query("""
            SELECT rt FROM RecurringTracker rt WHERE rt.hostelId=:hostelId AND rt.creationMonth=:month 
            AND rt.creationYear=:year
            """)
    List<RecurringTracker> findInvoiceGenerateForAMonth(String hostelId, int month, int year);
}
