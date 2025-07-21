package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentStatusRepository extends JpaRepository<PaymentStatus, Integer> {

}
