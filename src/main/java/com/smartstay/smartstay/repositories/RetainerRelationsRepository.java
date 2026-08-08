package com.smartstay.smartstay.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RetainerRelationsRepository extends JpaRepository<com.smartstay.smartstay.dao.RetainerRelations, Long> {
    com.smartstay.smartstay.dao.RetainerRelations findByInvoiceId(String invoiceId);
}
