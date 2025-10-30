package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.KycDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KycRepository extends JpaRepository<KycDetails, Long> {
}
