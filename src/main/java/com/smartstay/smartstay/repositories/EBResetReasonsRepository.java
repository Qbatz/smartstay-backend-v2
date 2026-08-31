package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.EBResetReasons;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EBResetReasonsRepository extends JpaRepository<EBResetReasons, Long> {
}
