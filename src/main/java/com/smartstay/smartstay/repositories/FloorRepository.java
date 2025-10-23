package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.Floors;
import com.smartstay.smartstay.dto.FloorsCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FloorRepository extends JpaRepository<Floors, Integer> {


    List<Floors> findAllByHostelId(String hostelId);

    List<Floors> findAllByHostelIdAndParentIdAndIsDeletedFalse(String hostelId, String parentId);


    Floors findByFloorId(int floorId);

    @Query(value = "SELECT * FROM floors where floor_id=:floorId and parent_id=:parentId", nativeQuery = true)
    Floors findByFloorIdAndParentId(@Param("floorId") int floorId, @Param("parentId")  String parentId);

    @Query(value = "SELECT * FROM floors where floor_id=:floorId and parent_id=:parentId and hostel_id=:hostelId", nativeQuery = true)
    Floors findByFloorIdAndParentIdAndHostelId(@Param("floorId") int floorId, @Param("parentId")  String parentId,@Param("hostelId")  String hostelId);

    Floors findByFloorIdAndHostelId(int floorId,String hostelId);

    @Query("SELECT COUNT(f) FROM Floors f WHERE f.floorName = :floorName AND f.hostelId = :hostelId AND f.parentId = :parentId AND f.isDeleted = false")
    int countByFloorNameAndRoomAndHostelAndParent(@Param("floorName") String floorName,
                                                @Param("hostelId") String hostelId,
                                                @Param("parentId") String parentId);

    @Query("SELECT COUNT(f) FROM Floors f WHERE f.floorName = :floorName AND f.floorId <> :floorId AND f.isDeleted = false AND f.hostelId = :hostelId")
    int countByFloorNameAndFloorId(@Param("floorName") String floorName,
                                   @Param("hostelId") String hostelId,
                                   @Param("floorId") Integer floorId);

    @Query(value = "select count(fl.floor_id) as count from floors fl where fl.hostel_id=:hostelId and fl.is_active=true and fl.is_deleted=false", nativeQuery = true)
    FloorsCount findFloorCountsBasedOnHostelId(@Param("hostelId") String hostelId);

    @Query("""
       SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
       FROM BookingsV1 b
       WHERE b.hostelId = :hostelId
       AND b.floorId = :floorId
       AND b.currentStatus IN (:statuses)
       """)
    boolean existsActiveBookingForFloor(@Param("hostelId") String hostelId,
                                        @Param("floorId") Integer floorId,
                                        @Param("statuses") List<String> statuses);


    @Query("""
       SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
       FROM BookingsV1 b
       WHERE b.hostelId = :hostelId
       AND b.roomId = :roomId
       AND b.currentStatus IN (:statuses)
       """)
    boolean existsActiveBookingForRoom(@Param("hostelId") String hostelId,
                                       @Param("roomId") Integer roomId,
                                       @Param("statuses") List<String> statuses);



}
