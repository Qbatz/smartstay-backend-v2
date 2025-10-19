package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.dao.TransactionV1;
import com.smartstay.smartstay.dto.bank.PaymentHistoryProjection;
import com.smartstay.smartstay.dto.transaction.Receipts;
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

        @Query(value = """
                SELECT invc.invoice_id as invoiceId, invc.invoice_number as invoiceNumber, 
                invc.invoice_mode as invoiceMode, invc.invoice_type as invoiceType, transaction.status as paymentStatus, 
                transaction.paid_amount as paidAmount, transaction.payment_date as paidAt, 
                transaction.transaction_id as transactionId, customers.first_name as firstName, customers.last_name as lastName, 
                transaction.reference_number as referenceNumber, transaction.bank_id as bankId, bank.bank_name as bankName, 
                bank.account_holder_name as holderName, bank.account_type as accountType, customers.profile_pic as profilePic FROM transactionv1 transaction 
                LEFT OUTER JOIN invoicesv1 invc on invc.invoice_id = transaction.invoice_id LEFT OUTER JOIN 
                customers customers on customers.customer_id=transaction.customer_id LEFT OUTER JOIN 
                bankingv1 bank on bank.bank_id=transaction.bank_id WHERE transaction.hostel_id=:hostelId
                """, nativeQuery = true)
        List<Receipts> findByHostelId(@Param("hostelId") String hostelId);

        boolean existsByTransactionReferenceId(String transactionReferenceId);


    @Query(value = """
    SELECT 
        reference_number AS referenceNumber,
        paid_amount AS amount,
        DATE_FORMAT(paid_at, '%d/%m/%Y') AS paidDate,
        transaction_reference_id as transactionReferenceId
    FROM 
        transactionv1
    WHERE 
        invoice_id = :invoiceId
    ORDER BY 
        paid_at DESC
""", nativeQuery = true)
    List<PaymentHistoryProjection> getPaymentHistoryByInvoiceId(@Param("invoiceId") String invoiceId);


}
