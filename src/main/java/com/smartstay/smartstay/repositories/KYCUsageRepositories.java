package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.KYCUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KYCUsageRepositories extends JpaRepository<KYCUsage, Long> {
    KYCUsage findByHostelId(String hostelId);
}
