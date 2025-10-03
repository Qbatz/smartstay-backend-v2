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

    ElectricityReadings findTopByRoomIdAndHostelIdOrderByEntryDateDesc(Integer roomId, String hostelId);

    @Query(value = """
            SELECT er.id, er.room_id as roomId, er.entry_date as entryDate, 
            er.current_unit_price as unitPrice, er.hostel_id as hostelId, flrs.floor_id as floorId, 
            rms.room_name as roomName, flrs.floor_name as floorName, (select sum(consumption) 
            from electricity_readings where room_id=er.room_id) as consumption, 
            (select id from electricity_readings where room_id=er.room_id order by entry_date desc LIMIT 1) as id, 
            (select current_reading from electricity_readings where room_id=er.room_id order by entry_date desc LIMIT 1) as currentReading,
            (SELECT count(booking_id) FROM bookingsv1 WHERE room_id=er.room_id and current_status in ('NOTICE', 'CHECKIN'))  as noOfTenants 
            FROM electricity_readings er LEFT OUTER JOIN rooms rms on rms.room_id=er.room_id 
            left outer join floors flrs on flrs.floor_id=rms.floor_id where er.hostel_id=:hostelId GROUP by er.room_id
            """, nativeQuery = true)
    List<ElectricityReaddings> getElectricity(@Param("hostelId") String hostelId);

    @Query(value = """
            SELECT er.room_id as roomId FROM electricity_readings er where er.hostel_id=:hostelId GROUP by er.room_id
            """, nativeQuery = true)
    List<Integer> getRoomIds(@Param("hostelId") String hostelId);

}
