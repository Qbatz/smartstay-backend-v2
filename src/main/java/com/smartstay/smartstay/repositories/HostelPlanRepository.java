package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.HostelPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface HostelPlanRepository extends JpaRepository<HostelPlan, Long> {
    @Query(value = """
            SELECT hp FROM HostelPlan hp WHERE hp.currentPlanEndsAt <= DATE(:todaysDate)
            """)
    List<HostelPlan> findNotActiveHostels(@Param("todaysDate") Date todaysDate);


}
