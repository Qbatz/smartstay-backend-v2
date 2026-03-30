package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.Plans;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlansRepository extends JpaRepository<Plans, Long> {
    Plans findPlanByPlanCode(String planCode);
    @Query(value = "SELECT * FROM plans WHERE plan_type=:planType limit 1", nativeQuery = true)
    Plans findPlanByPlanTypeAndIsActiveTrue(@Param("planType") String planType);

}
