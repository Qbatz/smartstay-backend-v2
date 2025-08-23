package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.ComplaintsV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<ComplaintsV1, String> {


    ComplaintsV1 findByComplaintIdAndParentId(int complaintId, String parentId);

    List<ComplaintsV1> findAllByHostelId(String hostelId);
}
