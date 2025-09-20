package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.ElectricityConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElectricityConfigRepository extends JpaRepository<ElectricityConfig, Integer> {
}
