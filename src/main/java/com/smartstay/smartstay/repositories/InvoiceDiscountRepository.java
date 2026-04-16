package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.InvoiceDiscounts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceDiscountRepository extends JpaRepository<InvoiceDiscounts, Long> {
//    InvoiceDiscounts findByHostelIdAndInvoiceIdAndIsActiveTrue(String hostelId, String invoiceId);


    Optional<InvoiceDiscounts> findFirstByHostelIdAndInvoiceIdOrderByDiscountIdDesc(String hostelId, String invoiceId);

    @Query("""
            SELECT ids FROM InvoiceDiscounts ids WHERE ids.hostelId=:hostelId AND ids.invoiceId IN (:invoiceIds)
            """)
    List<InvoiceDiscounts> findByHostelIdAndInvoiceIdIn(String hostelId, List<String> invoiceIds);
    InvoiceDiscounts findByHostelIdAndInvoiceIdAndIsActiveTrue(String hostelId, String invoiceId);
    @Query("""
            SELECT ids FROM InvoiceDiscounts ids WHERE ids.hostelId=:hostelId AND ids.invoiceId IN (:invoiceId) 
            AND ids.isActive=true 
            """)
    List<InvoiceDiscounts> findByHostelIdAndInvoiceIdsAndIsActive(String hostelId, List<String> invoiceId);
}
