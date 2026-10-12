package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.BankingIds;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankingIdsRepository extends JpaRepository<BankingIds, String> {
    @Query("""
            SELECT bi FROM BankingIds bi WHERE bi.bankIdV2=:v2BankId
            """)
    List<BankingIds> findByBankIdV2(String v2BankId);
}
