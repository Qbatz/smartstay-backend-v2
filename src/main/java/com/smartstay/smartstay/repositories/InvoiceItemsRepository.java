package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.InvoiceItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceItemsRepository extends JpaRepository<InvoiceItems, Long> {
    List<InvoiceItems> findByInvoice_InvoiceId(String invoiceId);
}
