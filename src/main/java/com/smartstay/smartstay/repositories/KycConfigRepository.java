package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.KycConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KycConfigRepository extends JpaRepository<KycConfig, Long> {
    KycConfig findByHostelId(String hostelId);
    @Query("""
            SELECT kyc FROM KycConfig kyc WHERE kyc.hostelId IN (:hostelIds) AND 
            kyc.canRequest = true
            """)
    List<KycConfig> findByHostelId(List<String> hostelIds);
}
