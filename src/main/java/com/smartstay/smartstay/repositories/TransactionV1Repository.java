package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.TransactionV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionV1Repository extends JpaRepository<TransactionV1, String> {
}
