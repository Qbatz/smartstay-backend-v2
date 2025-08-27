package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dto.beds.Beds;
import com.smartstay.smartstay.responses.beds.BedsStatusCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BedsRepository extends JpaRepository<com.smartstay.smartstay.dao.Beds, Integer> {

    List<com.smartstay.smartstay.dao.Beds> findAllByRoomIdAndParentId(int roomId, String parentId);

    com.smartstay.smartstay.dao.Beds findByBedIdAndParentId(int bedId, String parentId);
    com.smartstay.smartstay.dao.Beds findByBedIdAndRoomIdAndHostelId(int bedId, int RoomId, String hostelId);


    @Query("SELECT COUNT(b) FROM Beds b WHERE b.bedName = :bedName AND b.roomId = :roomId AND b.hostelId = :hostelId AND b.parentId = :parentId AND b.isDeleted = false")
    int countByBedNameAndRoomAndHostelAndParent(@Param("bedName") String bedName,
                                                @Param("roomId") Integer roomId,
                                                @Param("hostelId") String hostelId,
                                                @Param("parentId") String parentId);


    @Query("SELECT COUNT(b) FROM Beds b WHERE b.bedName = :bedName AND b.bedId != :bedId AND b.roomId = :roomId AND b.isDeleted = false")
    int countByBedNameAndBedId(@Param("bedName") String bedName,
                                                @Param("roomId") Integer bedId,@Param("roomId") Integer roomId);

    @Query(value = "SELECT b.status as status, COUNT(b.bed_id) as count FROM beds b WHERE b.hostel_id = :hostelId GROUP BY b.status", nativeQuery = true)
    List<BedsStatusCount> getBedCountByStatus(@Param("hostelId") String hostelId);

    @Query(value = "SELECT bed.bed_id as bedId, bed.hostel_id as hostelId, bed.is_active as isActive, bed.is_booked as isBooked, bed.rent_amount as roomRent, bed.room_id as roomId, bed.free_from as freeFrom, bed.bed_name as bedName, bed.status as status, booking.rent_amount as currentRent, booking.joining_date as joiningDate, booking.leaving_date as leavingDate, booking.booking_id as bookingId, booking.created_by as createdBy, cus.exp_joining_date as expectedJoinig, cus.joining_date as cusJoiningDate, cus.first_name as firstName, cus.last_name as lastName, cus.profile_pic as profilePic, booking.current_status as bookingStatus  FROM smart_stay.beds bed left outer join bookingsv1 booking on booking.bed_id=bed.bed_id left outer join customers cus on cus.customer_id=booking.customer_id where bed.bed_id=:bedId and bed.parent_id=:parentId order by booking.created_at limit 2", nativeQuery = true)
    List<Beds> getBedInfo(@Param("bedId") int bedId, @Param("parentId") String parentId);


}
