package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.InvoiceItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceItemsRepository extends JpaRepository<InvoiceItems, Long> {
}
