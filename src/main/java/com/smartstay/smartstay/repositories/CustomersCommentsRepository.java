package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.CustomersComments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomersCommentsRepository extends JpaRepository<CustomersComments, Long> {
}
