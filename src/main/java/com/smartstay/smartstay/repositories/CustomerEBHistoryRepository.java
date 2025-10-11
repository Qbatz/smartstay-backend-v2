package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.CustomersEbHistory;
import com.smartstay.smartstay.dto.electricity.ElectricityCustomersList;
import com.smartstay.smartstay.dto.electricity.RoomElectricityCustomersList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface CustomerEBHistoryRepository extends JpaRepository<CustomersEbHistory, Long> {

    @Query(value = """
           SELECT ebHis.id, ebHis.amount, ebHis.bed_id as bedId, ebHis.units as consumption, ebHis.start_date as startDate, 
           ebHis.end_date as endDate, ebHis.customer_id as customerId, cus.profile_pic as profilePic, cus.first_name as firstName, 
           cus.last_name as lastName, 
           bed.bed_name as bedName FROM customers_eb_history ebHis INNER JOIN customers cus on cus.customer_id=ebHis.customer_id 
           inner JOIN beds bed on bed.bed_id=ebHis.bed_id WHERE ebHis.room_id=:roomId and ebHis.start_date >= DATE(:startDate) 
           and ebHis.end_date <= DATE(:endDate) ORDER BY ebHis.end_date DESC
            """, nativeQuery = true)
    List<RoomElectricityCustomersList> getAllCustomersByRoomId(@Param("roomId") Integer roomId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query(value = """
            SELECT ebHis.customer_id as customerId, flrs.floor_name as floorName, rms.room_name as roomName, 
            bed.bed_name as bedName, cus.first_name as firstName, cus.last_name as lastName, cus.profile_pic as profilePic, 
            (SELECT amount from customers_eb_history h where h.customer_id=ebHis.customer_id ORDER BY h.end_date DESC LIMIT 1) as amount, 
            (SELECT bed_id from customers_eb_history h where h.customer_id=ebHis.customer_id ORDER BY h.end_date DESC LIMIT 1) as bedId, 
            (SELECT floor_id from customers_eb_history h where h.customer_id=ebHis.customer_id ORDER BY h.end_date DESC LIMIT 1) as floorId, 
            (SELECT room_id from customers_eb_history h where h.customer_id=ebHis.customer_id ORDER BY h.end_date DESC LIMIT 1) as roomId, 
            (SELECT start_date from customers_eb_history h where h.customer_id=ebHis.customer_id ORDER BY h.end_date DESC LIMIT 1) as startDate, 
            (SELECT end_date from customers_eb_history h where h.customer_id=ebHis.customer_id ORDER BY h.end_date DESC LIMIT 1) as endDate, 
            (SELECT id from customers_eb_history h where h.customer_id=ebHis.customer_id ORDER BY h.end_date DESC LIMIT 1) as id, 
            (SELECT units from customers_eb_history h where h.customer_id=ebHis.customer_id ORDER BY h.end_date DESC LIMIT 1) as consumption  
            FROM customers_eb_history ebHis LEFT OUTER JOIN beds bed on bed.bed_id=ebHis.bed_id LEFT OUTER join floors flrs on flrs.floor_id=ebHis.floor_id 
            LEFT OUTER JOIN rooms rms on rms.room_id=ebHis.room_id INNER JOIN customers cus on cus.customer_id=ebHis.customer_id where ebHis.room_id in (:roomIds) 
            GROUP BY ebHis.customer_id ORDER BY ebHis.end_date;
            """, nativeQuery = true)
    List<ElectricityCustomersList> fetchAllCustomersList(List<Integer> roomIds);
}
