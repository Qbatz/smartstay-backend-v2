package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.KycHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface KycHistoryRepository extends JpaRepository<KycHistory, Long> {
    @Query("""
    SELECT k FROM KycHistory k WHERE k.hostelId IN :hostelIds AND k.endDate IS NULL AND k.startDate < :todaysDate 
    AND k.startDate = (SELECT MAX(k2.startDate) FROM KycHistory k2 WHERE k2.hostelId = k.hostelId AND k2.endDate IS NULL
    AND k2.startDate < :todaysDate)
    """)
    List<KycHistory> findByHostelsById(List<String> hostelIds, Date todaysDate);
}
