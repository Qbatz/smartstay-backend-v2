package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.ElectricityReadings;
import com.smartstay.smartstay.dto.electricity.ElectricityReaddings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ElectricityReadingRepository extends JpaRepository<ElectricityReadings, Integer> {
    ElectricityReadings findTopByHostelIdOrderByEntryDateDesc(String hostelId);

    ElectricityReadings findTopByRoomIdOrderByEntryDateDesc(Integer roomId);

    @Query(value = """
            SELECT er.id, er.consumption, er.created_at as createdAt, er.current_reading as currentReading, 
            er.current_unit_price as unitPrice, er.entry_date as entryDate, er.hostel_id as hostelId, rms.floor_id as floorId,
            er.previous_reading as previousReadings, er.room_id as roomId, rms.room_name as roomName, 
            flr.floor_name as floorName, usr.first_name as firstName, usr.last_name as lastName 
            FROM electricity_readings er left outer join rooms rms on rms.room_id=er.room_id 
            left outer join floors flr on flr.floor_id=rms.floor_id left outer join 
            users usr on usr.user_id=er.created_by where er.hostel_id=:hostelId
            """, nativeQuery = true)
    List<ElectricityReaddings> getElectricity(@Param("hostelId") String hostelId);
}
