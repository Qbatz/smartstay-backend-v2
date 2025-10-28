package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.HostelPlanHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanHistoryRepository extends JpaRepository<HostelPlanHistory, Long> {
}
