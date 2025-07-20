package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.Floors;
import com.smartstay.smartstay.dao.Rooms;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Rooms,Integer> {

    List<Rooms> findAllByFloorId(int floorId);

    Rooms findByRoomId(int roomId);
}
