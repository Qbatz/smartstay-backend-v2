package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.Reasons;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReasonRepository extends JpaRepository<Reasons,String> {
}
