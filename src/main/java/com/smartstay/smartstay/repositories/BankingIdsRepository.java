package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.BankingIds;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankingIdsRepository extends JpaRepository<BankingIds, String> {
}
