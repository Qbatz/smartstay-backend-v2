package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.KycDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KycRepository extends JpaRepository<KycDetails, Long> {
    @Query("""
            SELECT kd FROM KycDetails kd WHERE kd.currentStatus = 'REQUESTED'
            """)
    List<KycDetails> findAllRequested();
}
