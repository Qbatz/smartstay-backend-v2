package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.CustomersBedHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerBedHistoryRespository extends JpaRepository<CustomersBedHistory, Long> {
}
