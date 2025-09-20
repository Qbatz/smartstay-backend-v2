package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.dto.invoices.Invoices;
import com.smartstay.smartstay.dto.transaction.Receipts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoicesV1Repository extends JpaRepository<InvoicesV1, String> {

    @Query(value = """
            select invc.invoice_id as invoiceId, invc.base_price as basePrice, invc.total_amount as totalAmount, invc.gst, invc.cgst, invc.sgst,
            invc.created_at as createdAt, invc.created_by as createdBy, invc.customer_id as customerId, 
            invc.hostel_id as hostelId, invc.invoice_generated_date as invoiceGeneratedAt, 
            invc.invoice_due_date as invoiceDueDate, invc.invoice_type as invoiceType, 
            invc.payment_status as paymentStatus, invc.updated_at as updatedAt, 
            invc.invoice_number as invoiceNumber, customers.first_name as firstName, customers.last_name as lastName, 
            advance.advance_amount as advanceAmount, advance.deductions as deductions,
            (SELECT sum(trans.paid_amount) FROM transactionv1 trans where trans.invoice_id=invc.invoice_id) as paidAmount  
            from invoicesv1 invc inner join customers customers on customers.customer_id=invc.customer_id 
            left outer join advance advance on advance.customer_id=invc.customer_id where invc.hostel_id=:hostelId AND invc.invoice_type not in('BOOKING')
            """, nativeQuery = true)
    List<Invoices> findByHostelId(@Param("hostelId") String hostelId);

    @Query(value = """
            SELECT invc.invoice_id as invoiceId, invc.invoice_number as invoiceNumber, invc.invoice_mode as invoiceMode, invc.invoice_type as invoiceType, 
            invc.payment_status as paymentStatus, cus.profile_pic as profilePic,
            transaction.paid_amount as paidAmount, transaction.paid_at as paidAt, invc.customer_id as customerId, transaction.transaction_id as transactionId, cus.first_name as firstName, cus.last_name as lastName, 
            transaction.reference_number as referenceNumber,
            transaction.bank_id as bankId, bank.bank_name as bankName, bank.account_holder_name as holderName 
            FROM invoicesv1 invc 
            inner join transactionv1 transaction on transaction.invoice_id=invc.invoice_id 
            inner join customers cus on cus.customer_id=invc.customer_id 
            inner join bankingv1 bank on bank.bank_id=transaction.bank_id 
            where invc.hostel_id=:hostelId and invc.payment_status in ('PARTIAL_PAYMENT', 'PAID')
            """, nativeQuery = true)
    List<Receipts> findReceipts(@Param("hostelId") String hostelId);

    @Query(value = """
    SELECT * FROM invoicesv1
    WHERE invoice_number LIKE CONCAT(:prefix, '%')
    ORDER BY CAST(SUBSTRING(invoice_number, LENGTH(:prefix) + 2) AS UNSIGNED) DESC
    LIMIT 1
""", nativeQuery = true)
    InvoicesV1 findLatestInvoiceByPrefix(@Param("prefix") String prefix);

    InvoicesV1 findByCustomerIdAndHostelIdAndInvoiceType(String customerId, String hostelId, String invoiceType);
}
