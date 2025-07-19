package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.Floors;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FloorRepository extends JpaRepository<Floors, Integer> {


    List<Floors> findAllByHostelId(String hostelId);

    Floors findByFloorId(int floorId);
}
