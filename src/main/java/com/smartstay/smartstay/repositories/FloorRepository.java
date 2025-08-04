package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.Floors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FloorRepository extends JpaRepository<Floors, Integer> {


    List<Floors> findAllByHostelId(String hostelId);

    List<Floors> findAllByHostelIdAndParentId(String hostelId,String parentId);

    Floors findByFloorId(int floorId);

    @Query(value = "SELECT * FROM smart_stay.floors where floor_id=:floorId and parent_id=:parentId", nativeQuery = true)
    Floors findByFloorIdAndParentId(@Param("floorId") int floorId, @Param("parentId")  String parentId);

    Floors findByFloorIdAndHostelId(int floorId,String hostelId);
}
