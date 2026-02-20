package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dto.beds.*;
import com.smartstay.smartstay.responses.beds.BedsStatusCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface BedsRepository extends JpaRepository<com.smartstay.smartstay.dao.Beds, Integer> {

    List<com.smartstay.smartstay.dao.Beds> findAllByRoomIdAndParentId(int roomId, String parentId);

    com.smartstay.smartstay.dao.Beds findByBedIdAndParentId(int bedId, String parentId);

    com.smartstay.smartstay.dao.Beds findByBedIdAndParentIdAndHostelId(int bedId, String parentId, String hostelId);

    com.smartstay.smartstay.dao.Beds findByBedIdAndRoomIdAndParentId(int bedId, int roomId, String parentId);

    com.smartstay.smartstay.dao.Beds findByBedIdAndRoomIdAndHostelId(int bedId, int RoomId, String hostelId);

    List<com.smartstay.smartstay.dao.Beds> findByHostelIdAndIsDeletedFalse(String hostelId);
    @Query("SELECT COUNT(b) FROM Beds b WHERE b.bedName = :bedName AND b.roomId = :roomId AND b.hostelId = :hostelId AND b.parentId = :parentId AND b.isDeleted = false")
    int countByBedNameAndRoomAndHostelAndParent(@Param("bedName") String bedName, @Param("roomId") Integer roomId, @Param("hostelId") String hostelId, @Param("parentId") String parentId);
    @Query("SELECT COUNT(b) FROM Beds b WHERE b.bedName = :bedName AND b.bedId != :bedId AND b.roomId = :roomId AND b.isDeleted = false")
    int countByBedNameAndBedId(@Param("bedName") String bedName, @Param("bedId") Integer bedId, @Param("roomId") Integer roomId);

    @Query(value = "SELECT b.status as status, COUNT(b.bed_id) as count FROM beds b WHERE b.hostel_id = :hostelId GROUP BY b.status", nativeQuery = true)
    List<BedsStatusCount> getBedCountByStatus(@Param("hostelId") String hostelId);

    List<com.smartstay.smartstay.dao.Beds> findByBedNameIgnoreCaseAndBedIdNot(String bedName, Integer bedId);

    @Query(value = """
            SELECT bed.bed_id as bedId, bed.hostel_id as hostelId, bed.is_active as isActive, 
            bed.is_booked as isBooked, bed.rent_amount as roomRent, bed.room_id as roomId, bed.free_from as freeFrom, 
            bed.bed_name as bedName, bed.status as status, booking.rent_amount as currentRent, 
            booking.joining_date as joiningDate, booking.leaving_date as leavingDate, booking.booking_id as bookingId, 
            booking.created_by as createdBy, booking.expected_joining_date as expectedJoinig, 
            cus.joining_date as cusJoiningDate, cus.customer_id as customerId, cus.first_name as firstName, 
            cus.last_name as lastName, cus.profile_pic as profilePic, booking.current_status as bookingStatus,
             cus.mobile, flr.floor_id as floorId, flr.floor_name as floorName, rms.room_name as roomName, 
             cntry.country_code countryCode FROM beds bed left outer join bookingsv1 booking on booking.bed_id=bed.bed_id 
             left outer join customers cus on " + "cus.customer_id=booking.customer_id 
             left outer join rooms rms on rms.room_id=bed.room_id left outer join floors flr on flr.floor_id=rms.floor_id 
             left outer join countries cntry on cntry.country_id=cus.country where bed.bed_id=:bedId 
             and bed.parent_id=:parentId " + "order by  booking.created_at DESC limit 2""", nativeQuery = true)
    List<Beds> getBedInfo(@Param("bedId") int bedId, @Param("parentId") String parentId);

    @Query(value = """
                SELECT 
                b.bed_id as bedId, 
                b.bed_name as bedName, b.rent_amount as rentAmount, floor.floor_id as floorId, b.room_id as roomId,
                b.current_status as bedStatus, 
                ab.expected_joining_date AS expectedJoiningDate,
                nb.leaving_date AS leavingDate,
                room.room_name as roomName,
                floor.floor_name as floorName
            FROM beds b
            inner join rooms room on b.room_id = room.room_id
            inner join floors floor on floor.floor_id=room.floor_id
            LEFT JOIN (
                SELECT bk.bed_id, bk.expected_joining_date,
                       ROW_NUMBER() OVER (PARTITION BY bk.bed_id ORDER BY bk.leaving_date DESC) rn
                FROM bookingsv1 bk
                WHERE bk.current_status = 'BOOKED' 
            ) ab ON b.bed_id = ab.bed_id AND ab.rn = 1
            LEFT JOIN (
                SELECT bk.bed_id, bk.leaving_date,
                       ROW_NUMBER() OVER (PARTITION BY bk.bed_id ORDER BY bk.leaving_date DESC) rn
                FROM bookingsv1 bk
                WHERE bk.current_status = 'NOTICE' 
            ) nb ON b.bed_id = nb.bed_id AND nb.rn = 1 where b.hostel_id=:hostelId
                        """, nativeQuery = true)
    List<FreeBeds> getFreeBeds(@Param("hostelId") String hostelId);

    @Query(value = """
            SELECT b.bed_id as bedId, flr.floor_id as floorId, rms.room_id as roomId, 
            b.rent_amount as rentAmount, b.bed_name as bedName, flr.floor_name as floorName, 
            rms.room_name as roomName, b.current_status as currentStatus, b.is_booked as isBooked FROM beds b 
            inner join rooms rms on rms.room_id=b.room_id inner join floors flr on flr.floor_id= rms.floor_id 
            where b.current_status in ('NOTICE', 'VACANT', 'CANCELLED') AND (b.free_from IS NULL OR DATE(b.free_from) <= DATE(:joiningDate)) 
            and b.hostel_id=:hostelId and b.is_active=true
            """, nativeQuery = true)
    List<InitializeBooking> getFreeBeds(@Param("hostelId") String hostelId, @Param("joiningDate") Date joiningDate);

    @Query(value = """
            SELECT * FROM beds WHERE current_status in ('NOTICE', 'VACANT') AND hostel_id=:hostelId
            """, nativeQuery = true)
    List<com.smartstay.smartstay.dao.Beds> getAvailableBeds(@Param("hostelId") String hostelId);

    @Query(value = """
           SELECT * FROM beds b WHERE b.bed_id=:bedId and b.current_status in ('NOTICE', 'VACANT') and b.is_active=true
            """, nativeQuery = true)
    com.smartstay.smartstay.dao.Beds checkBedAvailability(@Param("bedId") Integer bedId);

    @Query(value = """
            SELECT b.bed_id as bedId, b.bed_name as bedName, flrs.floor_id as floorId,
            flrs.floor_name as floorName, rms.room_id as roomId, rms.room_name as roomName
             FROM beds b left outer join rooms rms on rms.room_id=b.room_id 
            left outer join floors flrs on flrs.floor_id = rms.floor_id where b.bed_id=:bedId
            """, nativeQuery = true)
    BedInformations getBedInformation(@Param("bedId") Integer bedId);


    @Query(value = """
            SELECT * FROM beds where current_status != 'OCCUPIED' and (free_from IS NULL OR free_from <= DATE(:joiningDate)) and bed_id=:bedId
            """, nativeQuery = true)
    com.smartstay.smartstay.dao.Beds checkIsBedAvailable(@Param("bedId") Integer bedId, @Param("joiningDate")  Date joiningDate);

    @Query(value = """
            SELECT bed.bed_id as bedId, bed.bed_name as bedName, flrs.floor_id as floorId, 
            flrs.floor_name as floorName, rms.room_id as roomId, rms.room_name as roomName 
            FROM beds bed left outer JOIN rooms rms on rms.room_id=bed.room_id LEFT OUTER JOIN 
            floors flrs on flrs.floor_id=rms.floor_id where bed.bed_id=:bedId;
            """, nativeQuery = true)
    BedDetails findByBedId(@Param("bedId") Integer bedId);

    @Query("SELECT b.roomId AS roomId, r.floorId AS floorId " +
            "FROM Beds b JOIN Rooms r ON b.roomId = r.roomId " +
            "WHERE b.bedId = :bedId AND b.hostelId = :hostelId")
    BedRoomFloor findRoomAndFloorByBedIdAndHostelId(@Param("bedId") Integer bedId,
                                                    @Param("hostelId") String hostelId);
    @Query(value = """
            SELECT bed.bed_id as bedId, flrs.floor_name as floorName, rms.room_name as roomName, 
            flrs.floor_id as floorId, rms.room_id as roomId FROM beds bed LEFT OUTER JOIN rooms rms on rms.room_id=bed.room_id 
            LEFT OUTER JOIN floors flrs on flrs.floor_id=rms.floor_id where bed.bed_id in (:listBedIds)
            """, nativeQuery = true)
    List<FloorNameRoomName> getBedNameRoomName(@Param("listBedIds") List<Integer> listBedIds);

    @Query(value = """
            SELECT bed.bed_id as bedId, bed.bed_name as bedName, flrs.floor_id as floorId, 
            flrs.floor_name as floorName, rms.room_id as roomId, rms.room_name as roomName 
            FROM beds bed left outer JOIN rooms rms on rms.room_id=bed.room_id LEFT OUTER JOIN 
            floors flrs on flrs.floor_id=rms.floor_id where bed.bed_id IN (:bedIds)
            """, nativeQuery = true)
    List<BedDetails> findByBedIds(@Param("bedIds") List<Integer> bedIds);

        @Query("SELECT COUNT(b) FROM Beds b WHERE b.hostelId = :hostelId AND b.isDeleted = false")
        int countAllByHostelId(@Param("hostelId") String hostelId);

        @Query("SELECT COUNT(b) FROM Beds b WHERE b.hostelId = :hostelId AND b.currentStatus = 'OCCUPIED' AND b.isDeleted = false")
        int countOccupiedByHostelId(@Param("hostelId") String hostelId);

        @Query("SELECT b.roomId as roomId, COUNT(b) as bedCount FROM Beds b WHERE b.hostelId = :hostelId GROUP BY b.roomId")
        List<RoomBedCount> countBedsByRoomForHostel(
                @Param("hostelId") String hostelId);

        List<com.smartstay.smartstay.dao.Beds> findByRoomIdIn(List<Integer> roomIds);

        @Query(value = """
                SELECT * FROM beds b WHERE b.hostel_id=:hostelId and b.current_status in ('NOTICE', 'OCCUPIED', 'BOOKED') and b.is_active=true and b.is_deleted=false
                """, nativeQuery = true)
        List<com.smartstay.smartstay.dao.Beds> findFilledBeds(String hostelId);
}
