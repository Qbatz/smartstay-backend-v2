package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.Rooms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomRepository extends JpaRepository<Rooms,Integer> {

    List<Rooms> findAllByFloorId(int floorId);

    List<Rooms> findAllByFloorIdAndParentId(int floorId, String parentId);

    Rooms findByRoomId(int roomId);

    Rooms findByRoomIdAndParentId(int roomId,String parentId);
    Rooms findByRoomIdAndParentIdAndHostelId(int roomId, String parentId, String hostelId);

    @Query(value = """
    SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END FROM rooms rm
    INNER JOIN floors fl ON rm.floor_id = fl.floor_id
    WHERE rm.room_id = :roomId AND rm.parent_id = :parentId AND fl.hostel_id = :hostelId
    """, nativeQuery = true)
    int checkRoomExistInTable(@Param("roomId") int roomId,
                              @Param("parentId") String parentId,
                              @Param("hostelId") String hostelId);


    Rooms findByRoomIdAndFloorId(int roomId,int floorId);



}
