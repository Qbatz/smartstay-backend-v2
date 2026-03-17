package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.CreditDebitNotes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditDebitNotesRepository extends JpaRepository<CreditDebitNotes, Integer> {
    List<CreditDebitNotes> findByInvoiceId(String invoiceId);
    List<CreditDebitNotes> findByInvoiceIdIn(List<String> invoiceIds);
}
