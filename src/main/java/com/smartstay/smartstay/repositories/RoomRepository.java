package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.Rooms;
import com.smartstay.smartstay.responses.rooms.RoomInfoForEB;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Rooms,Integer> {

    List<Rooms> findAllByFloorId(int floorId);

    List<Rooms> findAllByFloorIdAndParentId(int floorId, String parentId);

    Rooms findByRoomId(int roomId);

    Rooms findByRoomIdAndParentId(int roomId,String parentId);
    Rooms findByRoomIdAndParentIdAndHostelId(int roomId, String parentId, String hostelId);
    Rooms findByRoomIdAndParentIdAndHostelIdAndFloorId(int roomId, String parentId, String hostelId,int floorId);

    @Query(value = """
    SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END FROM rooms rm
    INNER JOIN floors fl ON rm.floor_id = fl.floor_id
    WHERE rm.room_id = :roomId AND rm.parent_id = :parentId AND fl.hostel_id = :hostelId
    """, nativeQuery = true)
    int checkRoomExistInTable(@Param("roomId") int roomId,
                              @Param("parentId") String parentId,
                              @Param("hostelId") String hostelId);


    Rooms findByRoomIdAndFloorId(int roomId,int floorId);


    @Query("SELECT COUNT(r) FROM Rooms r WHERE r.roomName = :roomName AND r.floorId = :floorId AND r.hostelId = :hostelId AND r.parentId = :parentId AND r.isDeleted = false")
    int countByRoomNameAndRoomAndHostelAndParent(@Param("roomName") String roomName,
                                                @Param("floorId") Integer floorId,
                                                @Param("hostelId") String hostelId,
                                                @Param("parentId") String parentId);

    @Query("SELECT COUNT(r) FROM Rooms r WHERE r.roomName = :roomName AND r.roomId != :roomId AND r.floorId = :floorId AND r.isDeleted = false")
    int countByRoomNameAndRoomId(@Param("roomName") String roomName,
                               @Param("floorId") Integer floorId,@Param("roomId") Integer roomId);


    @Query("SELECT count(r) FROM Rooms r where r.hostelId=:hostelId and r.isActive=true and r.isDeleted=false")
    int getCountOfRoomsBasedOnHostel(@Param("hostelId") String hostelId);

    @Query(value = """
            SELECT rms.floor_id as floorId, rms.room_id as roomId, rms.room_name as roomName, flrs.floor_name as floorName, 
            flrs.hostel_id as hostelId, (SELECT count(booking_id) FROM bookingsv1 WHERE room_id=rms.room_id and current_status in ('NOTICE', 'CHECKIN'))  as noOfTenants 
            FROM rooms rms left outer join floors flrs on flrs.floor_id=rms.floor_id 
            WHERE rms.room_id not in (:roomIds) and rms.hostel_id=:hostelId and rms.is_active=true and rms.is_deleted=false
            """, nativeQuery = true)
    List<RoomInfoForEB> getAllRoomsNotInEb(@Param("roomIds")  List<Integer> roomIds, @Param("hostelId") String hostelId);

    @Query(value = """
            SELECT rms.floor_id as floorId, rms.room_id as roomId, rms.room_name as roomName, 
            flrs.floor_name as floorName, flrs.hostel_id as hostelId, 
            (SELECT count(booking_id) FROM bookingsv1 WHERE room_id=rms.room_id and current_status in ('NOTICE', 'CHECKIN')) as noOfTenants 
            FROM rooms rms left outer join floors flrs on flrs.floor_id=rms.floor_id 
            WHERE rms.hostel_id=:hostelId and rms.is_active=true and rms.is_deleted=false
            """, nativeQuery = true)
    List<RoomInfoForEB> getAllRoomsForEb(@Param("hostelId") String hostelId);

    @Query(value = """
           SELECT rms.floor_id as floorId, rms.room_id as roomId, rms.room_name as roomName, 
           flrs.floor_name as floorName, rms.hostel_id as hostelId, 
           (SELECT count(booking_id) FROM bookingsv1 WHERE room_id=rms.room_id and current_status in ('NOTICE', 'CHECKIN'))  as noOfTenants 
           FROM rooms rms left outer join floors flrs on flrs.floor_id=rms.floor_id 
           where rms.hostel_id=:hostelId and rms.is_active=true and rms.is_deleted=false;
            """, nativeQuery = true)
    List<RoomInfoForEB> getAllRoomsByHostelForEB(@Param("hostelId") String hostelId);


}
