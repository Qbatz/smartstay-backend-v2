package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.Beds;
import com.smartstay.smartstay.responses.beds.BedsStatusCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BedsRepository extends JpaRepository<Beds, Integer> {

    List<Beds> findAllByRoomIdAndParentId(int roomId, String parentId);

    Beds findByBedIdAndParentId(int bedId, String parentId);
    Beds findByBedIdAndRoomIdAndHostelId(int bedId, int RoomId, String hostelId);

    @Query(value = "SELECT b.status as status, COUNT(b.bed_id) as count FROM beds b WHERE b.hostel_id = :hostelId GROUP BY b.status", nativeQuery = true)
    List<BedsStatusCount> getBedCountByStatus(@Param("hostelId") String hostelId);

}
