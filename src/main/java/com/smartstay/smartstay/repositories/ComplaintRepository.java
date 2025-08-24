package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.ComplaintsV1;
import com.smartstay.smartstay.responses.complaint.ComplaintResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<ComplaintsV1, String> {


    ComplaintsV1 findByComplaintIdAndParentId(int complaintId, String parentId);

    @Query(value = """
        SELECT 
            c.complaint_id AS complaintId,
            c.customer_id AS customerId,
            ct.complaint_type_id AS complaintTypeId,
            ct.complaint_type_name AS complaintTypeName,
            c.floor_id AS floorId,
            c.room_id AS roomId,
            c.complaint_date AS complaintDate,
            c.description AS description
        FROM complaints_v1 c
        JOIN complaint_type_v1 ct 
            ON c.complaint_type_id = ct.complaint_type_id
        WHERE c.hostel_id = :hostelId
        """, nativeQuery = true)
    List<ComplaintResponse> getAllComplaintsWithType(@Param("hostelId") String hostelId);

    @Query(value = """
        SELECT 
            c.complaint_id AS complaintId,
            c.complaint_date AS complaintDate,
            c.status AS status,
            ct.complaint_type_id AS complaintTypeId,
            ct.complaint_type_name AS complaintTypeName
        FROM complaints_v1 c
        JOIN complaint_type_v1 ct 
            ON c.complaint_type_id = ct.complaint_type_id
        WHERE c.complaint_id = :complaintId
          AND c.parent_id = :parentId
        """, nativeQuery = true)
    ComplaintResponse getComplaintsWithType(@Param("complaintId") int complaintId,
                                            @Param("parentId") String parentId);
    List<ComplaintsV1> findAllByHostelId(String hostelId);
}
