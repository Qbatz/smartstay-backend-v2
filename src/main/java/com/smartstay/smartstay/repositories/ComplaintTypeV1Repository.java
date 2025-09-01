package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.ComplaintTypeV1;
import com.smartstay.smartstay.responses.complaint.ComplaintTypeResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ComplaintTypeV1Repository extends JpaRepository<ComplaintTypeV1, Integer> {

    @Query("""
       SELECT 
           c.complaintTypeId AS complaintTypeId, 
           c.complaintTypeName AS complaintTypeName
       FROM ComplaintTypeV1 c
       WHERE c.hostelId = :hostelId
       """)
    List<ComplaintTypeResponse> getAllComplaintsType(@Param("hostelId") String hostelId);


    ComplaintTypeV1 findByComplaintTypeNameAndHostelIdAndParentId(String complaintTypeName,String hostelId, String parentId);

    ComplaintTypeV1 findByComplaintTypeNameAndHostelIdAndParentIdAndComplaintTypeIdNot(
            String complaintTypeName,
            String hostelId,
            String parentId,
            int complaintTypeId
    );


}
