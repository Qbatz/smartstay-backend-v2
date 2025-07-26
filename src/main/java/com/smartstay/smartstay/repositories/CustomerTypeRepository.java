package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.CustomersType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerTypeRepository extends JpaRepository<CustomersType, Integer> {
}
