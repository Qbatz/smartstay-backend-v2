package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.RecurringConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecurringConfigRepositories extends JpaRepository<RecurringConfiguration, Long> {
    RecurringConfiguration findByHostelId(String hostelId);

    @Query("""
            SELECT config FROM RecurringConfiguration config WHERE config.hostelId IN (:hostelIds)
            """)
    List<RecurringConfiguration> findByHostelIds(List<String> hostelIds);
}
