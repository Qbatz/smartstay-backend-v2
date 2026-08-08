package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.InvoiceNotes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceNotesRepository extends JpaRepository<InvoiceNotes, Long> {
    InvoiceNotes findByInvoiceId(String invoiceId);
}
