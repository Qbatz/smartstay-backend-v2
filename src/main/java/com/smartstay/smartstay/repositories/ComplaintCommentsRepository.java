package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.ComplaintComments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintCommentsRepository extends JpaRepository<ComplaintComments, String> {

    List<ComplaintComments> findByComplaint_ComplaintId(Integer complaintId);


}
