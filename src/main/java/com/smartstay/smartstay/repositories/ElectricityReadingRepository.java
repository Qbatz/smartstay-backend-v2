package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dto.electricity.ElectricityReadingForRoom;
import com.smartstay.smartstay.dto.electricity.ElectricityReadings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ElectricityReadingRepository extends JpaRepository<com.smartstay.smartstay.dao.ElectricityReadings, Integer> {
    com.smartstay.smartstay.dao.ElectricityReadings findTopByHostelIdOrderByEntryDateDesc(String hostelId);

    com.smartstay.smartstay.dao.ElectricityReadings findTopByRoomIdAndHostelIdOrderByEntryDateDesc(Integer roomId, String hostelId);

    @Query(value = """
            SELECT MAX(er.id) as id, er.room_id as roomId, MAX(er.entry_date) as entryDate, er.bill_start_date as startDate, er.bill_end_date as endDate,
            er.current_unit_price as unitPrice, er.hostel_id as hostelId, flrs.floor_id as floorId, er.current_reading as currentReading, 
            rms.room_name as roomName, flrs.floor_name as floorName, (select sum(e2.consumption) 
            from electricity_readings e2 where e2.room_id=er.room_id and e2.bill_start_date >= DATE(:startDate) 
            and e2.bill_end_date <= DATE(:endDate)) as consumption, 
            (SELECT count(booking_id) FROM bookingsv1 WHERE room_id=er.room_id and current_status in ('NOTICE', 'CHECKIN') and joining_date <= DATE(:endDate) 
            and leaving_date is null or leaving_date >= DATE(:endDate))  as noOfTenants 
            FROM electricity_readings er LEFT OUTER JOIN rooms rms on rms.room_id=er.room_id 
            left outer join floors flrs on flrs.floor_id=rms.floor_id where er.hostel_id=:hostelId 
            and er.bill_start_date >= DATE(:startDate) and er.bill_end_date <= DATE(:endDate) 
            GROUP by er.room_id ORDER BY entryDate DESC
            """, nativeQuery = true)
    List<ElectricityReadings> getElectricity(@Param("hostelId") String hostelId, @Param("startDate") String startDate, @Param("endDate") String endDate);


    @Query(value = """
            SELECT er.room_id as roomId FROM electricity_readings er where er.hostel_id=:hostelId GROUP by er.room_id
            """, nativeQuery = true)
    List<Integer> getRoomIds(@Param("hostelId") String hostelId);

    @Query(value = """
            SELECT er.id, er.consumption, er.current_reading as currentReading, er.current_unit_price as unitPrice, er.entry_date as entryDate, er.hostel_id as hostelId, er.room_id as roomId, er.bill_start_date as startDate FROM electricity_readings er WHERE er.room_id=:roomId
            """, nativeQuery = true)
    List<ElectricityReadingForRoom> getRoomReading(@Param("roomId") String roomId);

}
