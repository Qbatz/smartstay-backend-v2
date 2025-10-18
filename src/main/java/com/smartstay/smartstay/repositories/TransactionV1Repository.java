package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.dao.TransactionV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionV1Repository extends JpaRepository<TransactionV1, String> {

        List<TransactionV1> findByCustomerId(String customerId);

        List<TransactionV1> findByInvoiceId(String invoiceId);

        List<TransactionV1> findByInvoiceIdIn(List<String> invoiceId);

        @Query("SELECT COALESCE(SUM(t.paidAmount), 0) FROM TransactionV1 t WHERE t.invoiceId = :invoiceId")
        Double getTotalPaidAmountByInvoiceId(@Param("invoiceId") String invoiceId);



}
