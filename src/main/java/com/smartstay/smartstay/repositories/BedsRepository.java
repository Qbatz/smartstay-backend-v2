package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.Beds;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BedsRepository extends JpaRepository<Beds, Integer> {

    List<Beds> findAllByRoomIdAndParentId(int roomId, String parentId);

    Beds findByBedIdAndParentId(int bedId, String parentId);
    Beds findByBedIdAndRoomIdAndHostelId(int bedId, int RoomId, String hostelId);


}
