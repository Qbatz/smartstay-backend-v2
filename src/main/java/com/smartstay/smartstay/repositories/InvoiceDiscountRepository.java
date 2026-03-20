package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.InvoiceDiscounts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceDiscountRepository extends JpaRepository<InvoiceDiscounts, Long> {
    InvoiceDiscounts findByHostelIdAndInvoiceId(String hostelId, String invoiceId);
}
