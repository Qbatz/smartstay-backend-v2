package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.Plans;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlansRepository extends JpaRepository<Plans, Long> {
    Plans findPlanByPlanCode(String planCode);
    Plans findPlanByPlanType(String planType);
}
