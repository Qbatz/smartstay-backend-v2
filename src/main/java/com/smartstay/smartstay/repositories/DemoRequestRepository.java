package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.DemoRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DemoRequestRepository extends JpaRepository<DemoRequest, Long> {
}
