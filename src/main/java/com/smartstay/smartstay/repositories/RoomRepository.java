package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.Rooms;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Rooms,Integer> {

    List<Rooms> findAllByFloorId(int floorId);

    List<Rooms> findAllByFloorIdAndParentId(int floorId, String parentId);

    Rooms findByRoomId(int roomId);

    Rooms findByRoomIdAndParentId(int roomId,String parentId);

    Rooms findByRoomIdAndFloorId(int roomId,int floorId);



}
