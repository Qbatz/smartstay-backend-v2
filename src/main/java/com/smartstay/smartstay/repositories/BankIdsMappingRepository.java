package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.BankIdsMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankIdsMappingRepository extends JpaRepository<BankIdsMapping, Integer> {
}
