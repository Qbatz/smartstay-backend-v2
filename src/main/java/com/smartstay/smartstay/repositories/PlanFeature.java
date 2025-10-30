package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.PlanFeatures;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanFeature extends JpaRepository<PlanFeatures, Long> {
}
