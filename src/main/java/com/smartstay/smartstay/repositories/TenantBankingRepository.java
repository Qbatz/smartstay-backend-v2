package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.TenantBanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantBankingRepository extends JpaRepository<TenantBanking, Long> {

    TenantBanking findByCustomerId(String customerId);
}
