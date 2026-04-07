package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.CustomerRecurringTracker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRecurringTrackerRepository extends JpaRepository<CustomerRecurringTracker, Long> {
}
