package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.HotelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelTypeRepository extends JpaRepository<HotelType, Integer> {
    HotelType findByType(String type);
}
