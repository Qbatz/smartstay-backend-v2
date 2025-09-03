package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.ComplaintComments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplaintCommentsRepository extends JpaRepository<ComplaintComments, String> {


}
