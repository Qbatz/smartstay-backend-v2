package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.AddressTypes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressTypeRepository extends JpaRepository<AddressTypes, Integer> {
    AddressTypes findByType(String type);
}
